package sn.bmbacke.rh.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.bmbacke.rh.entity.Position;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
public interface PositionRepository extends GenericRepository<Position, Long> {
    /**
     * Trouve les positions par département
     */
    List<Position> findByDepartment_Id(Long departmentId);

    /**
     * Trouve les positions actives
     */
    List<Position> findByActiveTrue();

    /**
     * Trouve les positions par titre (exact)
     */
    Optional<Position> findByTitle(String title);

    /**
     * Trouve les positions avec un salaire dans la plage spécifiée
     */
    List<Position> findByMinSalaryGreaterThanEqualAndMaxSalaryLessThanEqual(
            BigDecimal minSalary, BigDecimal maxSalary);

    /**
     * Recherche des positions par titre ou description
     */
    @Query("SELECT p FROM Position p WHERE " +
            "(:keyword IS NULL OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:active IS NULL OR p.active = :active) AND " +
            "(:departmentId IS NULL OR p.department.id = :departmentId)")
    Page<Position> searchPositions(
            @Param("keyword") String keyword,
            @Param("active") Boolean active,
            @Param("departmentId") Long departmentId,
            Pageable pageable);

    /**
     * Vérifie si une position avec ce titre existe déjà (sauf celle avec l'ID spécifié)
     */
    boolean existsByTitleAndIdNot(String title, Long id);

    /**
     * Compte les positions par département
     */
    long countByDepartment_Id(Long departmentId);
}
