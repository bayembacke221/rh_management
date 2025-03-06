package sn.bmbacke.rh.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.bmbacke.rh.payload.dto.*;

import java.math.BigDecimal;
import java.util.List;

public interface PositionService {

    /**
     * Récupère toutes les positions avec pagination
     */
    Page<PositionDTO> getAllPositions(Pageable pageable);

    /**
     * Récupère une position par son identifiant
     */
    PositionDTO getPositionById(Long id);

    /**
     * Crée une nouvelle position
     */
    PositionDTO createPosition(PositionCreateDTO positionCreateDTO);

    /**
     * Met à jour une position existante
     */
    PositionDTO updatePosition(Long id, PositionUpdateDTO positionUpdateDTO);

    /**
     * Supprime une position
     */
    void deletePosition(Long id);

    /**
     * Vérifie si une position existe
     */
    boolean positionExists(Long id);

    /**
     * Récupère les positions d'un département
     */
    List<PositionDTO> getPositionsByDepartment(Long departmentId);

    /**
     * Récupère les positions actives
     */
    List<PositionDTO> getActivePositions();

    /**
     * Active ou désactive une position
     */
    PositionDTO updatePositionStatus(Long id, Boolean active);

    /**
     * Recherche des positions par critères
     */
    Page<PositionDTO> searchPositions(String keyword, Boolean active, Long departmentId, Pageable pageable);

    /**
     * Récupère les positions dans une plage de salaire
     */
    List<PositionDTO> getPositionsBySalaryRange(BigDecimal minSalary, BigDecimal maxSalary);

    /**
     * Vérifie si un titre de position existe déjà (sauf pour la position avec l'ID spécifié)
     */
    boolean titleExists(String title, Long excludeId);
}