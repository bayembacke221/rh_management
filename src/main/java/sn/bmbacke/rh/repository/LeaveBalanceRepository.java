package sn.bmbacke.rh.repository;

import sn.bmbacke.rh.entity.LeaveBalance;
import sn.bmbacke.rh.entity.enums.LeaveType;

import java.util.List;
/**
 * Repository pour les soldes de congés
 */
public interface LeaveBalanceRepository extends GenericRepository<LeaveBalance, Long> {

    /**
     * Récupère le solde de congés d'un employé pour un type et une année donnés
     */
    LeaveBalance findByEmployee_IdAndLeaveTypeAndYear(
            Long employeeId, LeaveType leaveType, Integer year);

    /**
     * Récupère tous les soldes de congés d'un employé
     */
    List<LeaveBalance> findByEmployee_Id(Long employeeId);

    /**
     * Récupère tous les soldes de congés d'un employé pour une année donnée
     */
    List<LeaveBalance> findByEmployee_IdAndYear(Long employeeId, Integer year);

    /**
     * Récupère tous les soldes d'un type de congé pour une année donnée
     */
    List<LeaveBalance> findByLeaveTypeAndYear(LeaveType leaveType, Integer year);
}

