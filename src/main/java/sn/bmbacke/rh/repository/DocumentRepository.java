package sn.bmbacke.rh.repository;

import sn.bmbacke.rh.entity.Document;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sn.bmbacke.rh.entity.enums.DocEnum;

import java.util.List;

public interface DocumentRepository extends GenericRepository<Document, Long> {

    /**
     * Trouve les documents d'un employé
     */
    List<Document> findByEmployee_Id(Long employeeId);

    /**
     * Trouve les documents d'un contrat
     */
    List<Document> findByContract_Id(Long contractId);

    /**
     * Trouve les documents par type
     */
    List<Document> findByType(DocEnum type);

    /**
     * Recherche de documents par nom
     */
    @Query("SELECT d FROM Document d WHERE " +
            "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Document> searchDocuments(@Param("keyword") String keyword);

    /**
     * Compte les documents par type
     */
    long countByType(DocEnum type);

    /**
     * Compte les documents par employé
     */
    long countByEmployee_Id(Long employeeId);

    /**
     * Trouve les documents d'un employé par type
     */
    List<Document> findByEmployee_IdAndType(Long employeeId, DocEnum type);

    /**
     * Vérifie si un document d'un certain type existe pour un employé
     */
    boolean existsByEmployee_IdAndType(Long employeeId, DocEnum type);
}
