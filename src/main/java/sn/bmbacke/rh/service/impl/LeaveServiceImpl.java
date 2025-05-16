package sn.bmbacke.rh.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.bmbacke.rh.entity.Employee;
import sn.bmbacke.rh.entity.Leave;
import sn.bmbacke.rh.entity.LeaveBalance;
import sn.bmbacke.rh.entity.LeavePolicy;
import sn.bmbacke.rh.entity.enums.LeaveStatus;
import sn.bmbacke.rh.entity.enums.LeaveType;
import sn.bmbacke.rh.event.LeaveStatusChangedEvent;
import sn.bmbacke.rh.event.SystemEventPublisher;
import sn.bmbacke.rh.exception.BusinessException;
import sn.bmbacke.rh.exception.ResourceNotFoundException;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.payload.mapper.LeaveBalanceMapper;
import sn.bmbacke.rh.payload.mapper.LeaveMapper;
import sn.bmbacke.rh.repository.EmployeeRepository;
import sn.bmbacke.rh.repository.LeaveBalanceRepository;
import sn.bmbacke.rh.repository.LeavePolicyRepository;
import sn.bmbacke.rh.repository.LeaveRepository;
import sn.bmbacke.rh.service.LeaveBalanceService;
import sn.bmbacke.rh.service.LeavePolicyService;
import sn.bmbacke.rh.service.LeaveService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des congés
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final LeavePolicyService leavePolicyService;
    private final LeaveMapper leaveMapper;
    private final SystemEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveDTO> getAllLeaves(Pageable pageable) {
        return leaveRepository.findAll(pageable)
                .map(leaveMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveDTO getLeaveById(Long id) {
        return leaveRepository.findLeaveWithBasicRelationsById(id)
                .map(leaveMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Congé non trouvé avec l'ID : " + id));
    }

    @Override
    public LeaveDTO createLeave(LeaveCreateDTO leaveCreateDTO) {
        // Vérifier que l'employé existe
        Employee employee = employeeRepository.findById(leaveCreateDTO.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + leaveCreateDTO.getEmployeeId()));

        // Valider les dates de congé
        validateLeaveDates(leaveCreateDTO.getStartDate(), leaveCreateDTO.getEndDate(),
                leaveCreateDTO.getHalfDay(), leaveCreateDTO.getLeaveType());

        // Vérifier si l'employé a des congés qui se chevauchent
        if (hasOverlappingLeave(leaveCreateDTO.getEmployeeId(), leaveCreateDTO.getStartDate(),
                leaveCreateDTO.getEndDate(), null)) {
            throw new BusinessException("L'employé a déjà une demande de congé pour cette période");
        }

        // Vérifier les politiques de congés
        LeavePolicyDTO policy = leavePolicyService.getLeavePolicyByTypeAndYear(
                leaveCreateDTO.getLeaveType(), Year.now().getValue());

        if (policy != null) {
            validateLeaveRequest(leaveCreateDTO, policy);
        }

        // Vérifier le solde disponible
        if (leaveCreateDTO.getLeaveType() == LeaveType.ANNUAL_LEAVE ||
                leaveCreateDTO.getLeaveType() == LeaveType.SICK_LEAVE) {

            LeaveBalanceDTO balance = leaveBalanceService.getOrCreateLeaveBalance(
                    leaveCreateDTO.getEmployeeId(), leaveCreateDTO.getLeaveType(), Year.now().getValue());

            // Calculer le nombre de jours demandés
            int requestedDays = calculateRequestedDays(
                    leaveCreateDTO.getStartDate(), leaveCreateDTO.getEndDate(), leaveCreateDTO.getHalfDay());

            if (balance.getCurrentBalance() < requestedDays) {
                throw new BusinessException("Solde de congés insuffisant. " +
                        "Disponible: " + balance.getCurrentBalance() + ", Demandé: " + requestedDays);
            }
        }

        // Conversion et sauvegarde
        Leave leave = leaveMapper.toEntity(leaveCreateDTO);
        Leave savedLeave = leaveRepository.save(leave);

        // Publier un événement pour la notification
        LeaveStatusChangedEvent event = LeaveStatusChangedEvent.builder()
                .leaveId(savedLeave.getId())
                .employeeId(savedLeave.getEmployee().getId())
                .managerId(null)
                .oldStatus(null)
                .newStatus(savedLeave.getStatus())
                .eventTime(LocalDateTime.now())
                .triggeredBy(savedLeave.getEmployee().getId())
                .build();

        eventPublisher.publishEvent(event);

        return leaveMapper.toDto(savedLeave);
    }

    @Override
    public LeaveDTO updateLeave(Long id, LeaveUpdateDTO leaveUpdateDTO) {
        // Vérifier que le congé existe
        Leave existingLeave = leaveRepository.findLeaveWithBasicRelationsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Congé non trouvé avec l'ID : " + id));

        // Vérifier que le congé est toujours en attente
        if (existingLeave.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException("Seules les demandes en attente peuvent être modifiées");
        }

        // Valider les dates de congé
        validateLeaveDates(leaveUpdateDTO.getStartDate(), leaveUpdateDTO.getEndDate(),
                leaveUpdateDTO.getHalfDay(), leaveUpdateDTO.getLeaveType());

        // Vérifier si l'employé a des congés qui se chevauchent
        if (hasOverlappingLeave(existingLeave.getEmployee().getId(), leaveUpdateDTO.getStartDate(),
                leaveUpdateDTO.getEndDate(), id)) {
            throw new BusinessException("L'employé a déjà une demande de congé pour cette période");
        }

        // Vérifier les politiques de congés
        LeavePolicyDTO policy = leavePolicyService.getLeavePolicyByTypeAndYear(
                leaveUpdateDTO.getLeaveType(), Year.now().getValue());

        if (policy != null) {
            LeaveCreateDTO createDTO = new LeaveCreateDTO();
            createDTO.setStartDate(leaveUpdateDTO.getStartDate());
            createDTO.setEndDate(leaveUpdateDTO.getEndDate());
            createDTO.setLeaveType(leaveUpdateDTO.getLeaveType());
            createDTO.setHalfDay(leaveUpdateDTO.getHalfDay());

            validateLeaveRequest(createDTO, policy);
        }

        // Vérifier le solde disponible si le type de congé a changé ou les dates ont changé
        if ((leaveUpdateDTO.getLeaveType() == LeaveType.ANNUAL_LEAVE ||
                leaveUpdateDTO.getLeaveType() == LeaveType.SICK_LEAVE) &&
                (leaveUpdateDTO.getLeaveType() != existingLeave.getLeaveType() ||
                        !leaveUpdateDTO.getStartDate().equals(existingLeave.getStartDate()) ||
                        !leaveUpdateDTO.getEndDate().equals(existingLeave.getEndDate()) ||
                        leaveUpdateDTO.getHalfDay() != existingLeave.getHalfDay())) {

            LeaveBalanceDTO balance = leaveBalanceService.getOrCreateLeaveBalance(
                    existingLeave.getEmployee().getId(), leaveUpdateDTO.getLeaveType(), Year.now().getValue());

            // Calculer le nombre de jours demandés
            int requestedDays = calculateRequestedDays(
                    leaveUpdateDTO.getStartDate(), leaveUpdateDTO.getEndDate(), leaveUpdateDTO.getHalfDay());

            if (balance.getCurrentBalance() < requestedDays) {
                throw new BusinessException("Solde de congés insuffisant. " +
                        "Disponible: " + balance.getCurrentBalance() + ", Demandé: " + requestedDays);
            }
        }

        // Mise à jour
        leaveMapper.updateEntityFromDto(leaveUpdateDTO, existingLeave);
        Leave updatedLeave = leaveRepository.save(existingLeave);

        return leaveMapper.toDto(updatedLeave);
    }

    @Override
    public LeaveDTO updateLeaveStatus(Long id, LeaveApprovalDTO leaveApprovalDTO, Long approverId) {
        // Vérifier que le congé existe
        Leave existingLeave = leaveRepository.findLeaveWithBasicRelationsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Congé non trouvé avec l'ID : " + id));

        // Vérifier que le congé est en attente
        if (existingLeave.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException("Seules les demandes en attente peuvent être approuvées ou rejetées");
        }

        // Vérifier que l'approbateur existe
        Employee approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Approbateur non trouvé avec l'ID : " + approverId));

        // Garder l'ancien statut pour l'événement
        LeaveStatus oldStatus = existingLeave.getStatus();

        // Mise à jour du statut
        leaveMapper.updateApprovalFromDto(leaveApprovalDTO, existingLeave);
        existingLeave.setApprovedBy(approver);
        existingLeave.setApprovalDate(LocalDateTime.now());

        // Si approuvé, mettre à jour le solde de congés
        if (leaveApprovalDTO.getStatus() == LeaveStatus.APPROVED) {
            if (existingLeave.getLeaveType() == LeaveType.ANNUAL_LEAVE ||
                    existingLeave.getLeaveType() == LeaveType.SICK_LEAVE) {

                int year = existingLeave.getStartDate().getYear();
                int daysUsed = calculateRequestedDays(
                        existingLeave.getStartDate(), existingLeave.getEndDate(), existingLeave.getHalfDay());

                leaveBalanceService.updateUsedBalance(
                        existingLeave.getEmployee().getId(), existingLeave.getLeaveType(), year, (float) daysUsed);
            }
        }

        Leave updatedLeave = leaveRepository.save(existingLeave);

        // Publier un événement pour la notification
        LeaveStatusChangedEvent event = LeaveStatusChangedEvent.builder()
                .leaveId(updatedLeave.getId())
                .employeeId(updatedLeave.getEmployee().getId())
                .managerId(approverId)
                .oldStatus(oldStatus)
                .newStatus(updatedLeave.getStatus())
                .eventTime(LocalDateTime.now())
                .triggeredBy(approverId)
                .build();

        eventPublisher.publishEvent(event);

        return leaveMapper.toDto(updatedLeave);
    }

    @Override
    public LeaveDTO cancelLeave(Long id) {
        // Vérifier que le congé existe
        Leave existingLeave = leaveRepository.findLeaveWithBasicRelationsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Congé non trouvé avec l'ID : " + id));

        // Vérifier que le congé peut être annulé
        if (existingLeave.getStatus() != LeaveStatus.PENDING && existingLeave.getStatus() != LeaveStatus.APPROVED) {
            throw new BusinessException("Seules les demandes en attente ou approuvées peuvent être annulées");
        }

        // Garder l'ancien statut pour l'événement
        LeaveStatus oldStatus = existingLeave.getStatus();

        // Si le congé était approuvé, restaurer le solde
        if (existingLeave.getStatus() == LeaveStatus.APPROVED) {
            if (existingLeave.getLeaveType() == LeaveType.ANNUAL_LEAVE ||
                    existingLeave.getLeaveType() == LeaveType.SICK_LEAVE) {

                int year = existingLeave.getStartDate().getYear();
                int daysUsed = calculateRequestedDays(
                        existingLeave.getStartDate(), existingLeave.getEndDate(), existingLeave.getHalfDay());

                // Restaurer le solde en ajoutant un ajustement positif
                leaveBalanceService.updateUsedBalance(
                        existingLeave.getEmployee().getId(), existingLeave.getLeaveType(), year, (float) -daysUsed);
            }
        }

        // Mettre à jour le statut
        existingLeave.setStatus(LeaveStatus.CANCELLED);
        Leave updatedLeave = leaveRepository.save(existingLeave);

        // Publier un événement pour la notification
        LeaveStatusChangedEvent event = LeaveStatusChangedEvent.builder()
                .leaveId(updatedLeave.getId())
                .employeeId(updatedLeave.getEmployee().getId())
                .managerId(updatedLeave.getApprovedBy() != null ? updatedLeave.getApprovedBy().getId() : null)
                .oldStatus(oldStatus)
                .newStatus(updatedLeave.getStatus())
                .eventTime(LocalDateTime.now())
                .triggeredBy(updatedLeave.getEmployee().getId())
                .build();

        eventPublisher.publishEvent(event);

        return leaveMapper.toDto(updatedLeave);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveDTO> getEmployeeLeaves(Long employeeId, Pageable pageable) {
        // Vérifier que l'employé existe
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId);
        }

        return leaveRepository.findByEmployee_Id(employeeId, pageable)
                .map(leaveMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveDTO> getPendingLeavesByManager(Long managerId) {
        // Vérifier que le manager existe
        if (!employeeRepository.existsById(managerId)) {
            throw new ResourceNotFoundException("Manager non trouvé avec l'ID : " + managerId);
        }

        return leaveRepository.findPendingLeavesByManager(managerId).stream()
                .map(leaveMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaveDTO> searchLeaves(Long employeeId, LeaveStatus status, LeaveType leaveType,
                                       LocalDate startDateMin, LocalDate startDateMax, Long approvedById, Pageable pageable) {

        return leaveRepository.searchLeaves(employeeId, status, leaveType,
                        startDateMin, startDateMax, approvedById, pageable)
                .map(leaveMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveDTO> getLeavesInPeriod(LocalDate startDate, LocalDate endDate) {
        return leaveRepository.findLeavesInPeriod(startDate, endDate).stream()
                .map(leaveMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveDTO> getLeavesByDepartmentInPeriod(Long departmentId, LocalDate startDate, LocalDate endDate) {
        return leaveRepository.findLeavesByDepartmentInPeriod(departmentId, startDate, endDate).stream()
                .map(leaveMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasOverlappingLeave(Long employeeId, LocalDate startDate, LocalDate endDate, Long excludeLeaveId) {
        List<Leave> employeeLeaves = leaveRepository.findByEmployee_Id(employeeId);

        return employeeLeaves.stream()
                .filter(leave -> !leave.getStatus().equals(LeaveStatus.REJECTED) &&
                        !leave.getStatus().equals(LeaveStatus.CANCELLED))
                .filter(leave -> !leave.getId().equals(excludeLeaveId))
                .anyMatch(leave -> {
                    // Vérifier si les périodes se chevauchent
                    return (startDate.isBefore(leave.getEndDate()) || startDate.isEqual(leave.getEndDate())) &&
                            (endDate.isAfter(leave.getStartDate()) || endDate.isEqual(leave.getStartDate()));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveEmployeeSummaryDTO getEmployeeLeaveSummary(Long employeeId) {
        // Vérifier que l'employé existe
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId));

        int currentYear = Year.now().getValue();

        // Récupérer les congés de l'employé pour l'année en cours
        List<Leave> employeeLeaves = leaveRepository.findByEmployee_Id(employeeId);
        List<Leave> thisYearLeaves = employeeLeaves.stream()
                .filter(leave -> leave.getStartDate().getYear() == currentYear)
                .collect(Collectors.toList());

        // Compter le nombre total de congés cette année
        int totalLeavesThisYear = thisYearLeaves.size();

        // Compter le nombre de congés en attente
        int pendingLeaves = (int) thisYearLeaves.stream()
                .filter(leave -> leave.getStatus() == LeaveStatus.PENDING)
                .count();

        // Récupérer les soldes de congés
        LeaveBalanceDTO annualLeaveBalance = null;
        LeaveBalanceDTO sickLeaveBalance = null;

        try {
            annualLeaveBalance = leaveBalanceService.getOrCreateLeaveBalance(
                    employeeId, LeaveType.ANNUAL_LEAVE, currentYear);
        } catch (Exception e) {
            // Ignorer l'exception si le solde n'existe pas
        }

        try {
            sickLeaveBalance = leaveBalanceService.getOrCreateLeaveBalance(
                    employeeId, LeaveType.SICK_LEAVE, currentYear);
        } catch (Exception e) {
            // Ignorer l'exception si le solde n'existe pas
        }

        // Récupérer les congés à venir
        LocalDate today = LocalDate.now();
        List<LeaveDTO> upcomingLeaves = employeeLeaves.stream()
                .filter(leave -> leave.getStatus() == LeaveStatus.APPROVED)
                .filter(leave -> leave.getStartDate().isAfter(today) ||
                        leave.getStartDate().isEqual(today))
                .sorted((l1, l2) -> l1.getStartDate().compareTo(l2.getStartDate()))
                .limit(5)
                .map(leaveMapper::toDto)
                .collect(Collectors.toList());

        // Construire et retourner le résumé
        return LeaveEmployeeSummaryDTO.builder()
                .employee(EmployeeShortDTO.builder()
                        .id(employee.getId())
                        .firstName(employee.getFirstName())
                        .lastName(employee.getLastName())
                        .build())
                .totalLeavesThisYear(totalLeavesThisYear)
                .pendingLeaves(pendingLeaves)
                .annualLeaveBalance(annualLeaveBalance != null ? annualLeaveBalance.getCurrentBalance() : null)
                .sickLeaveBalance(sickLeaveBalance != null ? sickLeaveBalance.getCurrentBalance() : null)
                .upcomingLeaves(upcomingLeaves)
                .build();
    }

    /**
     * Valide les dates de congé
     */
    private void validateLeaveDates(LocalDate startDate, LocalDate endDate, Boolean halfDay, LeaveType leaveType) {
        if (startDate == null) {
            throw new BusinessException("La date de début est obligatoire");
        }

        if (endDate == null) {
            throw new BusinessException("La date de fin est obligatoire");
        }

        if (startDate.isAfter(endDate)) {
            throw new BusinessException("La date de début doit être antérieure ou égale à la date de fin");
        }

        if (Boolean.TRUE.equals(halfDay) && !startDate.isEqual(endDate)) {
            throw new BusinessException("Une demi-journée doit avoir la même date de début et de fin");
        }

        // Vérifications supplémentaires selon le type de congé
        LocalDate today = LocalDate.now();

        if (leaveType == LeaveType.ANNUAL_LEAVE) {
            // Pour les congés payés, vérifier qu'ils sont demandés à l'avance
            if (startDate.isBefore(today)) {
                throw new BusinessException("Les congés payés doivent être demandés avant la date de début");
            }
        } else if (leaveType == LeaveType.SICK_LEAVE) {
            // Pour les congés maladie, vérifier qu'ils ne sont pas trop dans le futur
            if (startDate.isAfter(today.plusDays(1))) {
                throw new BusinessException("Les congés maladie ne peuvent pas être demandés plus d'un jour à l'avance");
            }
        }
    }

    /**
     * Valide une demande de congé par rapport à la politique de congés
     */
    private void validateLeaveRequest(LeaveCreateDTO leaveCreateDTO, LeavePolicyDTO policy) {
        // Vérifier le nombre maximum de jours consécutifs
        if (policy.getMaxConsecutiveDays() != null) {
            int requestedDays = calculateRequestedDays(
                    leaveCreateDTO.getStartDate(), leaveCreateDTO.getEndDate(), leaveCreateDTO.getHalfDay());

            if (requestedDays > policy.getMaxConsecutiveDays()) {
                throw new BusinessException(
                        "Le nombre maximum de jours consécutifs pour ce type de congé est de " +
                                policy.getMaxConsecutiveDays());
            }
        }

        // Vérifier le préavis minimum
        if (policy.getMinRequestNoticeDays() != null) {
            LocalDate today = LocalDate.now();
            LocalDate minStartDate = today.plusDays(policy.getMinRequestNoticeDays());

            if (leaveCreateDTO.getStartDate().isBefore(minStartDate)) {
                throw new BusinessException(
                        "Ce type de congé doit être demandé au moins " +
                                policy.getMinRequestNoticeDays() + " jours à l'avance");
            }
        }

        // Vérifier si des documents sont requis
        if (Boolean.TRUE.equals(policy.getRequiresDocumentation())) {
            if (leaveCreateDTO.getAttachments() == null || leaveCreateDTO.getAttachments().isEmpty()) {
                throw new BusinessException("Ce type de congé nécessite des documents justificatifs");
            }
        }
    }

    /**
     * Calcule le nombre de jours demandés
     */
    private int calculateRequestedDays(LocalDate startDate, LocalDate endDate, Boolean halfDay) {
        long days = endDate.toEpochDay() - startDate.toEpochDay() + 1;

        if (Boolean.TRUE.equals(halfDay) && days == 1) {
            return 1;
        }

        return (int) days;
    }
}

