package sn.bmbacke.rh.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.bmbacke.rh.entity.enums.LeaveStatus;
import sn.bmbacke.rh.entity.enums.LeaveType;
import sn.bmbacke.rh.payload.dto.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface du service de gestion des congés
 */
public interface LeaveService {

    /**
     * Récupère toutes les demandes de congés avec pagination
     */
    Page<LeaveDTO> getAllLeaves(Pageable pageable);

    /**
     * Récupère une demande de congés par son ID
     */
    LeaveDTO getLeaveById(Long id);

    /**
     * Crée une nouvelle demande de congés
     */
    LeaveDTO createLeave(LeaveCreateDTO leaveCreateDTO);

    /**
     * Met à jour une demande de congés existante
     */
    LeaveDTO updateLeave(Long id, LeaveUpdateDTO leaveUpdateDTO);

    /**
     * Approuve ou rejette une demande de congés
     */
    LeaveDTO updateLeaveStatus(Long id, LeaveApprovalDTO leaveApprovalDTO, Long approverId);

    /**
     * Annule une demande de congés
     */
    LeaveDTO cancelLeave(Long id);

    /**
     * Récupère les demandes de congés d'un employé
     */
    Page<LeaveDTO> getEmployeeLeaves(Long employeeId, Pageable pageable);

    /**
     * Récupère les demandes de congés en attente pour un manager
     */
    List<LeaveDTO> getPendingLeavesByManager(Long managerId);

    /**
     * Recherche des demandes de congés selon différents critères
     */
    Page<LeaveDTO> searchLeaves(
            Long employeeId,
            LeaveStatus status,
            LeaveType leaveType,
            LocalDate startDateMin,
            LocalDate startDateMax,
            Long approvedById,
            Pageable pageable);

    /**
     * Récupère les congés pour une période donnée
     */
    List<LeaveDTO> getLeavesInPeriod(LocalDate startDate, LocalDate endDate);

    /**
     * Récupère les congés d'un département pour une période donnée
     */
    List<LeaveDTO> getLeavesByDepartmentInPeriod(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * Vérifie si un employé a des congés qui se chevauchent avec une période donnée
     */
    boolean hasOverlappingLeave(Long employeeId, LocalDate startDate, LocalDate endDate, Long excludeLeaveId);

    /**
     * Récupère un résumé des congés pour un employé
     */
    LeaveEmployeeSummaryDTO getEmployeeLeaveSummary(Long employeeId);
}

