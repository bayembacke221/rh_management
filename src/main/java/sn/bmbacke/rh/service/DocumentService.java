package sn.bmbacke.rh.service;


import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import sn.bmbacke.rh.payload.dto.DocumentDTO;
import sn.bmbacke.rh.payload.dto.DocumentShortDTO;
import sn.bmbacke.rh.payload.dto.DocumentUpdateDTO;
import sn.bmbacke.rh.entity.enums.DocEnum;

import java.util.List;

public interface DocumentService {

    /**
     * Récupère tous les documents avec pagination
     */
    Page<DocumentShortDTO> getAllDocuments(Pageable pageable);

    /**
     * Récupère un document par son identifiant
     */
    DocumentDTO getDocumentById(Long id);

    /**
     * Crée un nouveau document pour un employé
     */
    DocumentDTO uploadEmployeeDocument(Long employeeId, MultipartFile file, DocEnum type, String name);

    /**
     * Crée un nouveau document pour un contrat
     */
    DocumentDTO uploadContractDocument(Long contractId, MultipartFile file, DocEnum type, String name);

    /**
     * Met à jour les informations d'un document existant
     */
    DocumentDTO updateDocument(Long id, DocumentUpdateDTO documentUpdateDTO);

    /**
     * Supprime un document
     */
    void deleteDocument(Long id);

    /**
     * Récupère les documents d'un employé
     */
    List<DocumentShortDTO> getEmployeeDocuments(Long employeeId);

    /**
     * Récupère les documents d'un contrat
     */
    List<DocumentShortDTO> getContractDocuments(Long contractId);

    /**
     * Vérifie si un document existe
     */
    boolean documentExists(Long id);

    /**
     * Récupère le contenu d'un document
     */
    Resource getDocumentContent(Long id);

    /**
     * Récupère les documents par type
     */
    List<DocumentShortDTO> getDocumentsByType(DocEnum type);

    /**
     * Recherche des documents par nom
     */
    List<DocumentShortDTO> searchDocuments(String keyword);
}
