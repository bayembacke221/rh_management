package sn.bmbacke.rh.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.bmbacke.rh.entity.Employee;
import sn.bmbacke.rh.entity.LeaveBalance;
import sn.bmbacke.rh.entity.enums.LeaveType;
import sn.bmbacke.rh.exception.BusinessException;
import sn.bmbacke.rh.exception.ResourceNotFoundException;
import sn.bmbacke.rh.payload.dto.LeaveBalanceAdjustmentDTO;
import sn.bmbacke.rh.payload.dto.LeaveBalanceCreateDTO;
import sn.bmbacke.rh.payload.dto.LeaveBalanceDTO;
import sn.bmbacke.rh.payload.dto.LeavePolicyDTO;
import sn.bmbacke.rh.payload.mapper.LeaveBalanceMapper;
import sn.bmbacke.rh.repository.EmployeeRepository;
import sn.bmbacke.rh.repository.LeaveBalanceRepository;
import sn.bmbacke.rh.service.LeaveBalanceService;
import sn.bmbacke.rh.service.LeavePolicyService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des soldes de congés
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LeaveBalanceServiceImpl implements LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final LeavePolicyService leavePolicyService;
    private final LeaveBalanceMapper leaveBalanceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceDTO> getAllLeaveBalances() {
        return leaveBalanceRepository.findAll().stream()
                .map(leaveBalanceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveBalanceDTO getLeaveBalanceById(Long id) {
        return leaveBalanceRepository.findById(id)
                .map(leaveBalanceMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Solde de congés non trouvé avec l'ID : " + id));
    }

    @Override
    @Transactional
    public LeaveBalanceDTO getOrCreateLeaveBalance(Long employeeId, LeaveType leaveType, Integer year) {
        // Vérifier que l'employé existe
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId));

        // Rechercher le solde existant
        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployee_IdAndLeaveTypeAndYear(
                employeeId, leaveType, year);

        // Si le solde n'existe pas, le créer
        if (leaveBalance == null) {
            leaveBalance = new LeaveBalance();
            leaveBalance.setEmployee(employee);
            leaveBalance.setLeaveType(leaveType);
            leaveBalance.setYear(year);
            leaveBalance.setUsedBalance(0F);

            // Déterminer le solde initial selon la politique
            Float initialBalance = 0F;
            LeavePolicyDTO policy = leavePolicyService.getLeavePolicyByTypeAndYear(leaveType, year);
            if (policy != null) {
                initialBalance = policy.getDefaultDays();
            }

            leaveBalance.setInitialBalance(initialBalance);
            leaveBalance = leaveBalanceRepository.save(leaveBalance);
        }

        return leaveBalanceMapper.toDto(leaveBalance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceDTO> getEmployeeLeaveBalances(Long employeeId) {
        // Vérifier que l'employé existe
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId);
        }

        return leaveBalanceRepository.findByEmployee_Id(employeeId).stream()
                .map(leaveBalanceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceDTO> getEmployeeLeaveBalancesByYear(Long employeeId, Integer year) {
        // Vérifier que l'employé existe
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId);
        }

        return leaveBalanceRepository.findByEmployee_IdAndYear(employeeId, year).stream()
                .map(leaveBalanceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeaveBalanceDTO createLeaveBalance(LeaveBalanceCreateDTO leaveBalanceCreateDTO) {
        // Vérifier que l'employé existe
        Employee employee = employeeRepository.findById(leaveBalanceCreateDTO.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + leaveBalanceCreateDTO.getEmployeeId()));

        // Vérifier si un solde existe déjà pour ce type et cette année
        LeaveBalance existingBalance = leaveBalanceRepository.findByEmployee_IdAndLeaveTypeAndYear(
                leaveBalanceCreateDTO.getEmployeeId(), leaveBalanceCreateDTO.getLeaveType(), leaveBalanceCreateDTO.getYear());

        if (existingBalance != null) {
            throw new BusinessException("Un solde de congés existe déjà pour cet employé, ce type et cette année");
        }

        // Conversion et sauvegarde
        LeaveBalance leaveBalance = leaveBalanceMapper.toEntity(leaveBalanceCreateDTO);
        LeaveBalance savedLeaveBalance = leaveBalanceRepository.save(leaveBalance);

        return leaveBalanceMapper.toDto(savedLeaveBalance);
    }

    @Override
    @Transactional
    public LeaveBalanceDTO adjustLeaveBalance(Long id, LeaveBalanceAdjustmentDTO adjustmentDTO) {
        // Vérifier que le solde existe
        LeaveBalance leaveBalance = leaveBalanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solde de congés non trouvé avec l'ID : " + id));

        // Mettre à jour l'ajustement
        if (leaveBalance.getAdjustedBalance() == null) {
            leaveBalance.setAdjustedBalance(adjustmentDTO.getAdjustedAmount());
        } else {
            leaveBalance.setAdjustedBalance(leaveBalance.getAdjustedBalance() + adjustmentDTO.getAdjustedAmount());
        }

        leaveBalance.setAdjustmentReason(adjustmentDTO.getAdjustmentReason());

        LeaveBalance updatedLeaveBalance = leaveBalanceRepository.save(leaveBalance);
        return leaveBalanceMapper.toDto(updatedLeaveBalance);
    }

    @Override
    @Transactional
    public void updateUsedBalance(Long employeeId, LeaveType leaveType, Integer year, Float daysUsed) {
        // Récupérer ou créer le solde
        LeaveBalanceDTO balanceDTO = getOrCreateLeaveBalance(employeeId, leaveType, year);

        // Mettre à jour le solde utilisé
        LeaveBalance leaveBalance = leaveBalanceRepository.findById(balanceDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Solde de congés non trouvé avec l'ID : " + balanceDTO.getId()));

        leaveBalance.setUsedBalance(leaveBalance.getUsedBalance() + daysUsed);
        leaveBalanceRepository.save(leaveBalance);
    }

    @Override
    @Transactional
    public void initializeYearlyLeaveBalances(Integer year) {
        // Récupérer toutes les politiques de congés pour l'année
        List<LeavePolicyDTO> policies = leavePolicyService.getLeavePoliciesByYear(year);

        // Récupérer tous les employés actifs
        List<Employee> employees = employeeRepository.findAll();

        // Pour chaque politique et chaque employé
        for (LeavePolicyDTO policy : policies) {
            for (Employee employee : employees) {
                // Vérifier si un solde existe déjà
                LeaveBalance existingBalance = leaveBalanceRepository.findByEmployee_IdAndLeaveTypeAndYear(
                        employee.getId(), policy.getLeaveType(), year);

                if (existingBalance == null) {
                    // Créer un nouveau solde
                    LeaveBalance newBalance = new LeaveBalance();
                    newBalance.setEmployee(employee);
                    newBalance.setLeaveType(policy.getLeaveType());
                    newBalance.setYear(year);
                    newBalance.setInitialBalance(policy.getDefaultDays());
                    newBalance.setUsedBalance(0F);

                    // Vérifier le report de l'année précédente
                    if (Boolean.TRUE.equals(policy.getCarryForwardAllowed())) {
                        LeaveBalance previousYearBalance = leaveBalanceRepository.findByEmployee_IdAndLeaveTypeAndYear(
                                employee.getId(), policy.getLeaveType(), year - 1);

                        if (previousYearBalance != null) {
                            Float remainingBalance = previousYearBalance.getCurrentBalance();

                            // Limiter le report au maximum autorisé
                            if (policy.getMaxCarryForwardDays() != null && remainingBalance > policy.getMaxCarryForwardDays()) {
                                remainingBalance = policy.getMaxCarryForwardDays();
                            }

                            if (remainingBalance > 0) {
                                newBalance.setAdjustedBalance(remainingBalance);
                                newBalance.setAdjustmentReason("Report de l'année " + (year - 1));
                            }
                        }
                    }

                    leaveBalanceRepository.save(newBalance);
                }
            }
        }
    }
}
