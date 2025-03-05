package sn.bmbacke.rh.repository;

import sn.bmbacke.rh.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.bmbacke.rh.entity.Contract;
import sn.bmbacke.rh.entity.enums.Type;
import sn.bmbacke.rh.entity.enums.ContratStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import java.util.List;

public interface ContractRepository  extends GenericRepository<Contract, Long> {
    /**
     * Trouve les contrats d'un employé
     */
    List<Contract> findByEmployee_Id(Long employeeId);

    /**
     * Trouve le contrat actif d'un employé
     */
    Optional<Contract> findByEmployee_IdAndStatus(Long employeeId, ContratStatus status);

    /**
     * Trouve les contrats par type
     */
    List<Contract> findByType(Type type);

    /**
     * Trouve les contrats par statut
     */
    List<Contract> findByStatus(ContratStatus status);

    /**
     * Trouve les contrats qui expirent dans un certain nombre de jours
     */
    @Query("SELECT c FROM Contract c WHERE c.status = 'ACTIVE' AND c.endDate IS NOT NULL AND c.endDate BETWEEN :today AND :expiryDate")
    List<Contract> findExpiringContracts(@Param("today") LocalDate today, @Param("expiryDate") LocalDate expiryDate);

    /**
     * Recherche avancée de contrats
     */
    @Query("SELECT c FROM Contract c WHERE " +
            "(:keyword IS NULL OR " +
            "LOWER(c.employee.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.employee.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:type IS NULL OR c.type = :type) AND " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:startDateMin IS NULL OR c.startDate >= :startDateMin) AND " +
            "(:startDateMax IS NULL OR c.startDate <= :startDateMax)")
    Page<Contract> searchContracts(@Param("keyword") String keyword,
                                   @Param("type") Type type,
                                   @Param("status") ContratStatus status,
                                   @Param("startDateMin") LocalDate startDateMin,
                                   @Param("startDateMax") LocalDate startDateMax,
                                   Pageable pageable);
}
