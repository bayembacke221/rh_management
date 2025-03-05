package sn.bmbacke.rh.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.entity.enums.Status;

import java.util.List;

public interface EmployeeService {

    /**
     * Récupère tous les employés avec pagination
     */
    Page<EmployeeDTO> getAllEmployees(Pageable pageable);

    /**
     * Récupère les employés par département
     */
    Page<EmployeeDTO> getEmployeesByDepartement(Long departementId, Pageable pageable);

    /**
     * Récupère un employé par son identifiant
     */
    EmployeeDTO getEmployeeById(Long id);

    /**
     * Crée un nouvel employé
     */
    EmployeeDTO createEmployee(EmployeeCreateDTO employeeCreateDTO);

    /**
     * Met à jour un employé existant
     */
    EmployeeDTO updateEmployee(Long id, EmployeeUpdateDTO employeeUpdateDTO);

    /**
     * Met à jour uniquement le statut d'un employé
     */
    EmployeeDTO updateEmployeeStatus(Long id, EmployeeStatusUpdateDTO statusUpdateDTO);

    /**
     * Supprime un employé (soft delete)
     */
    void deleteEmployee(Long id);

    /**
     * Récupère les employés qui rapportent directement à un manager
     */
    List<EmployeeDTO> getSubordinates(Long managerId);

    /**
     * Recherche des employés par critères
     */
    Page<EmployeeDTO> searchEmployees(String keyword, Status status, Long departementId, Pageable pageable);

    /**
     * Vérifie si un employé existe
     */
    boolean employeeExists(Long id);

    /**
     * Récupère les employés récemment embauchés (30 derniers jours)
     */
    List<EmployeeDTO> getRecentHires();

    /**
     * Récupère le nombre total d'employés actifs
     */
    long getActiveEmployeeCount();

    /**
     * Récupère les statistiques des employés par département
     */
    List<DepartmentEmployeeStatsDTO> getEmployeeStatsByDepartment();
}