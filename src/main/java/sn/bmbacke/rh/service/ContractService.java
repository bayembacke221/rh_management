package sn.bmbacke.rh.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.entity.enums.Type;
import sn.bmbacke.rh.entity.enums.ContratStatus;

import java.time.LocalDate;
import java.util.List;

public interface ContractService {

    /**
     * Récupère tous les contrats avec pagination
     */
    Page<ContractDTO> getAllContracts(Pageable pageable);

    /**
     * Récupère un contrat par son identifiant
     */
    ContractDTO getContractById(Long id);

    /**
     * Crée un nouveau contrat
     */
    ContractDTO createContract(ContractCreateDTO contractCreateDTO);

    /**
     * Met à jour un contrat existant
     */
    ContractDTO updateContract(Long id, ContractUpdateDTO contractUpdateDTO);

    /**
     * Met à jour le statut d'un contrat
     */
    ContractDTO updateContractStatus(Long id, ContractStatusUpdateDTO statusUpdateDTO);

    /**
     * Supprime un contrat
     */
    void deleteContract(Long id);

    /**
     * Récupère les contrats d'un employé
     */
    List<ContractDTO> getEmployeeContracts(Long employeeId);

    /**
     * Vérifie si un contrat existe
     */
    boolean contractExists(Long id);

    /**
     * Récupère le contrat actif d'un employé (s'il en a un)
     */
    ContractDTO getActiveEmployeeContract(Long employeeId);

    /**
     * Récupère les contrats par type
     */
    List<ContractDTO> getContractsByType(Type type);

    /**
     * Récupère les contrats par statut
     */
    List<ContractDTO> getContractsByStatus(ContratStatus status);

    /**
     * Récupère les contrats qui expirent bientôt
     */
    List<ContractDTO> getExpiringContracts(int daysThreshold);

    /**
     * Génère un contrat à partir d'un modèle
     */
    byte[] generateContractDocument(Long contractId);

    /**
     * Recherche de contrats (par employé, date, etc.)
     */
    Page<ContractDTO> searchContracts(String keyword, Type type,
                                      ContratStatus status, LocalDate startDateMin,
                                      LocalDate startDateMax, Pageable pageable);
}
