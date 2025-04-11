package sn.bmbacke.rh.service;


import sn.bmbacke.rh.entity.enums.LeaveType;
import sn.bmbacke.rh.payload.dto.LeaveBalanceAdjustmentDTO;
import sn.bmbacke.rh.payload.dto.LeaveBalanceCreateDTO;
import sn.bmbacke.rh.payload.dto.LeaveBalanceDTO;

import java.util.List;

/**
 * Interface du service de gestion des soldes de congés
 */
public interface LeaveBalanceService {

    /**
     * Récupère tous les soldes de congés
     */
    List<LeaveBalanceDTO> getAllLeaveBalances();

    /**
     * Récupère un solde de congés par son ID
     */
    LeaveBalanceDTO getLeaveBalanceById(Long id);

    /**
     * Récupère ou crée le solde de congés d'un employé pour un type et une année donnés
     */
    LeaveBalanceDTO getOrCreateLeaveBalance(Long employeeId, LeaveType leaveType, Integer year);

    /**
     * Récupère tous les soldes de congés d'un employé
     */
    List<LeaveBalanceDTO> getEmployeeLeaveBalances(Long employeeId);

    /**
     * Récupère tous les soldes de congés d'un employé pour une année donnée
     */
    List<LeaveBalanceDTO> getEmployeeLeaveBalancesByYear(Long employeeId, Integer year);

    /**
     * Crée un nouveau solde de congés
     */
    LeaveBalanceDTO createLeaveBalance(LeaveBalanceCreateDTO leaveBalanceCreateDTO);

    /**
     * Ajuste un solde de congés existant
     */
    LeaveBalanceDTO adjustLeaveBalance(Long id, LeaveBalanceAdjustmentDTO adjustmentDTO);

    /**
     * Met à jour le solde utilisé après approbation d'un congé
     */
    void updateUsedBalance(Long employeeId, LeaveType leaveType, Integer year, Float daysUsed);

    /**
     * Initialise les soldes de congés pour tous les employés au début d'une nouvelle année
     */
    void initializeYearlyLeaveBalances(Integer year);
}