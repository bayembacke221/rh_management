package sn.bmbacke.rh.repository;

import sn.bmbacke.rh.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.bmbacke.rh.entity.Employee;
import sn.bmbacke.rh.entity.enums.Status;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeRepository extends GenericRepository<Employee, Long> {
    Page<Employee> findByDepartement_Id(Long departementId, Pageable pageable);

    List<Employee> findByManager_Id(Long managerId);

    List<Employee> findByHireDateBetween(LocalDate startDate, LocalDate endDate);

    Long countByStatus(Status status);

    @Query("SELECT e.departement.id as departmentId, " +
            "e.departement.name as departmentName, " +
            "e.departement.code as departmentCode, " +
            "COUNT(e) as totalEmployees, " +
            "SUM(CASE WHEN e.status = 'ACTIVE' THEN 1 ELSE 0 END) as activeEmployees, " +
            "SUM(CASE WHEN e.status = 'INACTIVE' THEN 1 ELSE 0 END) as inactiveEmployees, " +
            "SUM(CASE WHEN e.status = 'ON_LEAVE' THEN 1 ELSE 0 END) as onLeaveEmployees " +
            "FROM Employee e " +
            "WHERE e.departement IS NOT NULL " +
            "GROUP BY e.departement.id, e.departement.name, e.departement.code")
    List<Object[]> getEmployeeStatsByDepartment();

    @Query("SELECT e FROM Employee e WHERE " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:departementId IS NULL OR e.departement.id = :departementId) AND " +
            "(:keyword IS NULL OR " +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Employee> searchEmployees(
            @Param("keyword") String keyword,
            @Param("status") Status status,
            @Param("departementId") Long departementId,
            Pageable pageable);

    /**
     * Vérifie si un employé appartient à un département
     */
    boolean existsByDepartement_Id(Long departementId);
}
