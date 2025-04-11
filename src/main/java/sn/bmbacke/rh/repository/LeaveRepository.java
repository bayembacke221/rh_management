package sn.bmbacke.rh.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.bmbacke.rh.entity.Leave;
import sn.bmbacke.rh.entity.enums.LeaveStatus;
import sn.bmbacke.rh.entity.enums.LeaveType;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository pour les demandes de congés
 */
public interface LeaveRepository extends GenericRepository<Leave, Long> {

    /**
     * Récupère toutes les demandes de congés d'un employé
     */
    List<Leave> findByEmployee_Id(Long employeeId);

    /**
     * Récupère les demandes de congés d'un employé avec pagination
     */
    Page<Leave> findByEmployee_Id(Long employeeId, Pageable pageable);

    /**
     * Récupère les demandes de congés d'un employé par statut
     */
    List<Leave> findByEmployee_IdAndStatus(Long employeeId, LeaveStatus status);

    /**
     * Récupère les demandes de congés à approuver par un manager
     */
    @Query("SELECT l FROM Leave l WHERE l.status = 'PENDING' AND l.employee.manager.id = :managerId")
    List<Leave> findPendingLeavesByManager(@Param("managerId") Long managerId);

    /**
     * Recherche avancée de congés
     */
    @Query("SELECT l FROM Leave l WHERE " +
            "(:employeeId IS NULL OR l.employee.id = :employeeId) AND " +
            "(:status IS NULL OR l.status = :status) AND " +
            "(:leaveType IS NULL OR l.leaveType = :leaveType) AND " +
            "(:startDateMin IS NULL OR l.startDate >= :startDateMin) AND " +
            "(:startDateMax IS NULL OR l.startDate <= :startDateMax) AND " +
            "(:approvedById IS NULL OR l.approvedBy.id = :approvedById)")
    Page<Leave> searchLeaves(
            @Param("employeeId") Long employeeId,
            @Param("status") LeaveStatus status,
            @Param("leaveType") LeaveType leaveType,
            @Param("startDateMin") LocalDate startDateMin,
            @Param("startDateMax") LocalDate startDateMax,
            @Param("approvedById") Long approvedById,
            Pageable pageable);

    /**
     * Trouve les congés en cours pour une période donnée
     */
    @Query("SELECT l FROM Leave l WHERE " +
            "l.status = 'APPROVED' AND " +
            "((l.startDate BETWEEN :startDate AND :endDate) OR " +
            "(l.endDate BETWEEN :startDate AND :endDate) OR " +
            "(l.startDate <= :startDate AND l.endDate >= :endDate))")
    List<Leave> findLeavesInPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Trouve les congés en cours d'un département pour une période donnée
     */
    @Query("SELECT l FROM Leave l WHERE " +
            "l.status = 'APPROVED' AND " +
            "l.employee.departement.id = :departmentId AND " +
            "((l.startDate BETWEEN :startDate AND :endDate) OR " +
            "(l.endDate BETWEEN :startDate AND :endDate) OR " +
            "(l.startDate <= :startDate AND l.endDate >= :endDate))")
    List<Leave> findLeavesByDepartmentInPeriod(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Compte le nombre de jours de congés utilisés par un employé dans une période
     */
    @Query("SELECT SUM(l.durationDays) FROM Leave l WHERE " +
            "l.employee.id = :employeeId AND " +
            "l.status = 'APPROVED' AND " +
            "l.leaveType = :leaveType AND " +
            "YEAR(l.startDate) = :year")
    Integer countLeaveDaysByEmployeeAndTypeAndYear(
            @Param("employeeId") Long employeeId,
            @Param("leaveType") LeaveType leaveType,
            @Param("year") Integer year);

    /**
     * Vérifie si un employé a des congés qui se chevauchent avec une période donnée
     */
    @Query("SELECT COUNT(l) > 0 FROM Leave l WHERE " +
            "l.employee.id = :employeeId AND " +
            "l.status IN ('PENDING', 'APPROVED', 'IN_PROGRESS') AND " +
            "((l.startDate BETWEEN :startDate AND :endDate) OR " +
            "(l.endDate BETWEEN :startDate AND :endDate) OR " +
            "(l.startDate <= :startDate AND l.endDate >= :endDate))")
    boolean hasOverlappingLeave(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
