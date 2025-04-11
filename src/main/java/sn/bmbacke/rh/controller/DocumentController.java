package sn.bmbacke.rh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sn.bmbacke.rh.config.FileConfigProperties;
import sn.bmbacke.rh.payload.dto.DocumentDTO;
import sn.bmbacke.rh.payload.dto.DocumentShortDTO;
import sn.bmbacke.rh.payload.dto.DocumentUpdateDTO;
import sn.bmbacke.rh.entity.enums.DocEnum;
import sn.bmbacke.rh.helper.file.ResourceUtils;
import sn.bmbacke.rh.service.DocumentService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "API pour la gestion des documents dans le système RH")
public class DocumentController {

    private final DocumentService documentService;
    private final ResourceUtils resourceUtils;
    private final FileConfigProperties fileConfigProperties;

    @Operation(summary = "Récupérer tous les documents", description = "Retourne une liste paginée de tous les documents")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documents récupérés avec succès"),
            @ApiResponse(responseCode = "401", description = "Non autorisé", content = @Content)
    })
    @GetMapping("")
    public ResponseEntity<Page<DocumentShortDTO>> getAllDocuments(
            @PageableDefault(size = 20, sort = "uploadDate") Pageable pageable) {
        return ResponseEntity.ok(documentService.getAllDocuments(pageable));
    }

    @Operation(summary = "Récupérer un document par ID", description = "Retourne un document spécifique par son identifiant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document trouvé"),
            @ApiResponse(responseCode = "404", description = "Document non trouvé", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocumentById(
            @Parameter(description = "ID du document") @PathVariable("id")  Long id) {
        return ResponseEntity.ok(documentService.getDocumentById(id));
    }

    @Operation(summary = "Télécharger le contenu d'un document", description = "Télécharge le fichier associé à un document")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fichier téléchargé avec succès"),
            @ApiResponse(responseCode = "404", description = "Document non trouvé", content = @Content)
    })
    @GetMapping("/{id}/content")
    public ResponseEntity<Resource> getDocumentContent(
            @Parameter(description = "ID du document") @PathVariable("id")  Long id) {
        DocumentDTO document = documentService.getDocumentById(id);
        Resource resource = documentService.getDocumentContent(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"")
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .body(resource);
    }

    @Operation(summary = "Mettre à jour un document", description = "Met à jour les métadonnées d'un document existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document mis à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content),
            @ApiResponse(responseCode = "404", description = "Document non trouvé", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<DocumentDTO> updateDocument(
            @Parameter(description = "ID du document") @PathVariable("id")  Long id,
            @RequestBody DocumentUpdateDTO updateDTO) {
        return ResponseEntity.ok(documentService.updateDocument(id, updateDTO));
    }

    @Operation(summary = "Supprimer un document", description = "Supprime un document du système")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Document supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Document non trouvé", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "ID du document") @PathVariable("id")  Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Rechercher des documents", description = "Recherche des documents par mot-clé")
    @GetMapping("/search")
    public ResponseEntity<List<DocumentShortDTO>> searchDocuments(
            @Parameter(description = "Mot-clé pour la recherche") @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(documentService.searchDocuments(keyword));
    }

    @Operation(summary = "Récupérer les documents par type", description = "Retourne tous les documents d'un type spécifique")
    @GetMapping("/types/{type}")
    public ResponseEntity<List<DocumentShortDTO>> getDocumentsByType(
            @Parameter(description = "Type de document") @PathVariable("type")  DocEnum type) {
        return ResponseEntity.ok(documentService.getDocumentsByType(type));
    }

    @Operation(summary = "Récupérer les types MIME autorisés", description = "Retourne la liste des types MIME autorisés pour chaque type de document")
    @GetMapping("/mime-types")
    public ResponseEntity<Map<String, List<String>>> getAllowedMimeTypes() {
        return ResponseEntity.ok(fileConfigProperties.getUploads().getAllowedMimeTypes());
    }

    @Operation(summary = "Uploader un document pour un employé", description = "Télécharge un nouveau document et l'associe à un employé")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content),
            @ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content),
            @ApiResponse(responseCode = "415", description = "Type de média non supporté", content = @Content)
    })
    @PostMapping("/employees/{employeeId}")
    public ResponseEntity<DocumentDTO> uploadEmployeeDocument(
            @Parameter(description = "ID de l'employé") @PathVariable("employeeId") Long employeeId,
            @Parameter(description = "Fichier à uploader") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Type de document") @RequestParam("type") DocEnum type,
            @Parameter(description = "Nom du document (optionnel)") @RequestParam(value = "name", required = false) String name) {

        // Valider le type MIME
        if (!resourceUtils.isValidDocumentType(file, type)) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(null);
        }

        DocumentDTO document = documentService.uploadEmployeeDocument(employeeId, file, type, name);
        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }

    @Operation(summary = "Récupérer les documents d'un employé", description = "Retourne tous les documents associés à un employé")
    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<List<DocumentShortDTO>> getEmployeeDocuments(
            @Parameter(description = "ID de l'employé") @PathVariable("employeeId") Long employeeId) {
        return ResponseEntity.ok(documentService.getEmployeeDocuments(employeeId));
    }

    @Operation(summary = "Télécharger le contenu d'un document d'employé", description = "Télécharge le fichier d'un document associé à un employé")
    @GetMapping("/employees/{employeeId}/{documentId}/content")
    public ResponseEntity<Resource> getEmployeeDocumentContent(
            @Parameter(description = "ID de l'employé") @PathVariable("employeeId") Long employeeId,
            @Parameter(description = "ID du document") @PathVariable("documentId") Long documentId) {

        DocumentDTO document = documentService.getDocumentById(documentId);
        Resource resource = documentService.getDocumentContent(documentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getName() + "\"")
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .body(resource);
    }

    @Operation(summary = "Uploader un document pour un contrat", description = "Télécharge un nouveau document et l'associe à un contrat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Document créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content),
            @ApiResponse(responseCode = "404", description = "Contrat non trouvé", content = @Content),
            @ApiResponse(responseCode = "415", description = "Type de média non supporté", content = @Content)
    })
    @PostMapping("/contracts/{contractId}")
    public ResponseEntity<DocumentDTO> uploadContractDocument(
            @PathVariable("contractId") Long contractId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") DocEnum type,
            @RequestParam(value = "name", required = false) String name) {

        // Valider le type MIME
        if (!resourceUtils.isValidDocumentType(file, type)) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(null);
        }

        DocumentDTO document = documentService.uploadContractDocument(contractId, file, type, name);
        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }

    @Operation(summary = "Récupérer les documents d'un contrat", description = "Retourne tous les documents associés à un contrat")
    @GetMapping("/contracts/{contractId}")
    public ResponseEntity<List<DocumentShortDTO>> getContractDocuments(
            @Parameter(description = "ID du contrat") @PathVariable("contractId") Long contractId) {
        return ResponseEntity.ok(documentService.getContractDocuments(contractId));
    }
}