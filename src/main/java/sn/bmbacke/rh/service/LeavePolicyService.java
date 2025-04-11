package sn.bmbacke.rh.service;

import sn.bmbacke.rh.entity.enums.LeaveType;
import sn.bmbacke.rh.payload.dto.LeavePolicyCreateDTO;
import sn.bmbacke.rh.payload.dto.LeavePolicyDTO;

import java.util.List;

/**
 * Interface du service de gestion des politiques de congés
 */
public interface LeavePolicyService {

    /**
     * Récupère toutes les politiques de congés
     */
    List<LeavePolicyDTO> getAllLeavePolicies();

    /**
     * Récupère une politique de congés par son ID
     */
    LeavePolicyDTO getLeavePolicyById(Long id);

    /**
     * Récupère une politique de congés par type et année
     */
    LeavePolicyDTO getLeavePolicyByTypeAndYear(LeaveType leaveType, Integer year);

    /**
     * Récupère toutes les politiques de congés pour une année donnée
     */
    List<LeavePolicyDTO> getLeavePoliciesByYear(Integer year);

    /**
     * Crée une nouvelle politique de congés
     */
    LeavePolicyDTO createLeavePolicy(LeavePolicyCreateDTO leavePolicyCreateDTO);

    /**
     * Met à jour une politique de congés existante
     */
    LeavePolicyDTO updateLeavePolicy(Long id, LeavePolicyCreateDTO leavePolicyUpdateDTO);

    /**
     * Supprime une politique de congés
     */
    void deleteLeavePolicy(Long id);

    /**
     * Duplique les politiques de congés d'une année à l'autre
     */
    List<LeavePolicyDTO> duplicateLeavePolicies(Integer sourceYear, Integer targetYear);
}
