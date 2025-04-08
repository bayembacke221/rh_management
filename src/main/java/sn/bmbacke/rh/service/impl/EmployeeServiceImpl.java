package sn.bmbacke.rh.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.entity.Employee;
import sn.bmbacke.rh.entity.enums.Status;
import sn.bmbacke.rh.exception.ResourceNotFoundException;
import sn.bmbacke.rh.payload.mapper.EmployeeMapper;
import sn.bmbacke.rh.repository.DepartmentRepository;
import sn.bmbacke.rh.repository.EmployeeRepository;
import sn.bmbacke.rh.repository.PositionRepository;
import sn.bmbacke.rh.service.EmployeeService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departementRepository;
    private final PositionRepository positionRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(employeeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getEmployeesByDepartement(Long departementId, Pageable pageable) {
        // Vérifier si le département existe
        if (!departementRepository.existsById(departementId)) {
            throw new ResourceNotFoundException("Département non trouvé avec l'ID : " + departementId);
        }

        return employeeRepository.findByDepartement_Id(departementId, pageable)
                .map(employeeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .map(employeeMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + id));
    }

    @Override
    public EmployeeDTO createEmployee(EmployeeCreateDTO employeeCreateDTO) {
        // Vérifications pour les relations
        if (employeeCreateDTO.getDepartementId() != null &&
                !departementRepository.existsById(employeeCreateDTO.getDepartementId())) {
            throw new ResourceNotFoundException("Département non trouvé avec l'ID : " +
                    employeeCreateDTO.getDepartementId());
        }

        if (employeeCreateDTO.getPositionId() != null &&
                !positionRepository.existsById(employeeCreateDTO.getPositionId())) {
            throw new ResourceNotFoundException("Poste non trouvé avec l'ID : " +
                    employeeCreateDTO.getPositionId());
        }

        if (employeeCreateDTO.getManagerId() != null &&
                !employeeRepository.existsById(employeeCreateDTO.getManagerId())) {
            throw new ResourceNotFoundException("Manager non trouvé avec l'ID : " +
                    employeeCreateDTO.getManagerId());
        }

        // Conversion et sauvegarde
        Employee employee = employeeMapper.toEntity(employeeCreateDTO);

        // Valeurs par défaut
        if (employee.getStatus() == null) {
            employee.setStatus(Status.ACTIVE);
        }
        if (employee.getHireDate() == null) {
            employee.setHireDate(LocalDate.now());
        }

        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.toDto(savedEmployee);
    }

    @Override
    public EmployeeDTO updateEmployee(Long id, EmployeeUpdateDTO employeeUpdateDTO) {
        // Vérifier que l'employé existe
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + id));

        // Vérifications pour les relations
        if (employeeUpdateDTO.getDepartementId() != null &&
                !departementRepository.existsById(employeeUpdateDTO.getDepartementId())) {
            throw new ResourceNotFoundException("Département non trouvé avec l'ID : " +
                    employeeUpdateDTO.getDepartementId());
        }

        if (employeeUpdateDTO.getPositionId() != null &&
                !positionRepository.existsById(employeeUpdateDTO.getPositionId())) {
            throw new ResourceNotFoundException("Poste non trouvé avec l'ID : " +
                    employeeUpdateDTO.getPositionId());
        }

        if (employeeUpdateDTO.getManagerId() != null &&
                !employeeRepository.existsById(employeeUpdateDTO.getManagerId())) {
            throw new ResourceNotFoundException("Manager non trouvé avec l'ID : " +
                    employeeUpdateDTO.getManagerId());
        }

        // Éviter l'auto-référence comme manager
        if (employeeUpdateDTO.getManagerId() != null &&
                employeeUpdateDTO.getManagerId().equals(id)) {
            throw new IllegalArgumentException("Un employé ne peut pas être son propre manager");
        }

        // Mise à jour
        employeeMapper.updateEntityFromDto(employeeUpdateDTO, existingEmployee);
        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        return employeeMapper.toDto(updatedEmployee);
    }

    @Override
    public EmployeeDTO updateEmployeeStatus(Long id, EmployeeStatusUpdateDTO statusUpdateDTO) {
        // Vérifier que l'employé existe
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + id));

        // Mise à jour du statut
        existingEmployee.setStatus(statusUpdateDTO.getStatus());

        // En cas de fin de contrat
        if (statusUpdateDTO.getStatus() == Status.INACTIVE || statusUpdateDTO.getStatus() == Status.TERMINATED) {
            existingEmployee.setEndDate(statusUpdateDTO.getEndDate() != null ?
                    statusUpdateDTO.getEndDate() : LocalDate.now());
        }

        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        return employeeMapper.toDto(updatedEmployee);
    }

    @Override
    public void deleteEmployee(Long id) {
        // Vérifier que l'employé existe
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employé non trouvé avec l'ID : " + id);
        }

        // Soft delete (changement de statut) plutôt que suppression physique
        Employee employee = employeeRepository.getReferenceById(id);
        employee.setStatus(Status.TERMINATED);
        employee.setEndDate(LocalDate.now());
        employeeRepository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getSubordinates(Long managerId) {
        // Vérifier que le manager existe
        if (!employeeRepository.existsById(managerId)) {
            throw new ResourceNotFoundException("Manager non trouvé avec l'ID : " + managerId);
        }

        return employeeRepository.findByManager_Id(managerId).stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> searchEmployees(String keyword, Status status, Long departementId, Pageable pageable) {
        String sanitizedKeyword = keyword != null ? keyword.toString() : null;

        return employeeRepository.searchEmployees(sanitizedKeyword, status, departementId, pageable)
                .map(employeeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean employeeExists(Long id) {
        return employeeRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getRecentHires() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        return employeeRepository.findByHireDateBetween(thirtyDaysAgo, LocalDate.now()).stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveEmployeeCount() {
        return employeeRepository.countByStatus(Status.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentEmployeeStatsDTO> getEmployeeStatsByDepartment() {
        List<Object[]> statsData = employeeRepository.getEmployeeStatsByDepartment();
        List<DepartmentEmployeeStatsDTO> statsDTOs = new ArrayList<>();

        for (Object[] row : statsData) {
            DepartmentEmployeeStatsDTO statsDTO = DepartmentEmployeeStatsDTO.builder()
                    .departmentId((Long) row[0])
                    .departmentName((String) row[1])
                    .departmentCode((String) row[2])
                    .totalEmployees((Long) row[3])
                    .activeEmployees((Long) row[4])
                    .inactiveEmployees((Long) row[5])
                    .onLeaveEmployees((Long) row[6])
                    .build();
            statsDTOs.add(statsDTO);
        }

        return statsDTOs;
    }
}
