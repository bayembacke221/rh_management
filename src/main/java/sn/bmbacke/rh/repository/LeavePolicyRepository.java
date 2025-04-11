package sn.bmbacke.rh.repository;

import sn.bmbacke.rh.entity.LeavePolicy;
import sn.bmbacke.rh.entity.enums.LeaveType;

import java.util.List;

/**
 * Repository pour les politiques de congés
 */
public interface LeavePolicyRepository extends GenericRepository<LeavePolicy, Long> {

    /**
     * Récupère une politique de congés par type et année
     */
    LeavePolicy findByLeaveTypeAndYear(LeaveType leaveType, Integer year);

    /**
     * Récupère toutes les politiques de congés pour une année donnée
     */
    List<LeavePolicy> findByYear(Integer year);
}
