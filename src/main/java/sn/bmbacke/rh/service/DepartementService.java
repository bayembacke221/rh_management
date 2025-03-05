package sn.bmbacke.rh.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.bmbacke.rh.payload.dto.*;

import java.util.List;

public interface DepartementService {

    /**
     * Récupère tous les départements avec pagination
     */
    Page<DepartementDTO> getAllDepartements(Pageable pageable);

    /**
     * Récupère un département par son identifiant
     */
    DepartementDTO getDepartementById(Long id);

    /**
     * Créé un nouveau département
     */
    DepartementDTO createDepartement(DepartementCreateDTO departementCreateDTO);

    /**
     * Met à jour un département existant
     */
    DepartementDTO updateDepartement(Long id, DepartementUpdateDTO departementUpdateDTO);

    /**
     * Supprime un département
     */
    void deleteDepartement(Long id);

    /**
     * Vérifie si un département existe
     */
    boolean departementExists(Long id);

    /**
     * Récupère les employés d'un département
     */
    Page<EmployeeShortDTO> getEmployeesByDepartement(Long departementId, Pageable pageable);

    /**
     * Récupère la structure hiérarchique des départements
     */
    List<DepartementTreeDTO> getDepartementTree();

    /**
     * Récupère les départements de premier niveau (sans parent)
     */
    List<DepartementDTO> getTopLevelDepartements();

    /**
     * Récupère les sous-départements d'un département
     */
    List<DepartementDTO> getSubDepartements(Long parentId);

    /**
     * Active ou désactive un département
     */
    DepartementDTO updateDepartementStatus(Long id, Boolean active);

    /**
     * Recherche des départements par nom ou code
     */
    List<DepartementDTO> searchDepartements(String keyword);
}
