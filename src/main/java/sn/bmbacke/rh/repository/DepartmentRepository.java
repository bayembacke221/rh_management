package sn.bmbacke.rh.repository;

import sn.bmbacke.rh.entity.Departement;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DepartmentRepository extends GenericRepository<Departement, Long> {

    /**
     * Trouve les départements de premier niveau (sans parent)
     */
    List<Departement> findByParentDepartementIsNull();

    /**
     * Trouve les sous-départements d'un département
     */
    List<Departement> findByParentDepartement_Id(Long parentId);

    /**
     * Recherche des départements par nom ou code
     */
    @Query("SELECT d FROM Departement d WHERE " +
            "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Departement> searchDepartements(@Param("keyword") String keyword);

    /**
     * Vérifie si un département est utilisé par des employés
     */
    boolean existsByManager_Id(Long managerId);

    /**
     * Vérifie si un département a des sous-départements
     */
    boolean existsByParentDepartement_Id(Long parentId);

    /**
     * Trouve un département par son code
     */
    Departement findByCode(String code);
}
