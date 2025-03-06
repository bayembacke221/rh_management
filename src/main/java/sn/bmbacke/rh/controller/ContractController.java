package sn.bmbacke.rh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.entity.enums.ContratStatus;
import sn.bmbacke.rh.entity.enums.Type;
import sn.bmbacke.rh.service.ContractService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("contracts")
@RequiredArgsConstructor
@Tag(name = "Contrats", description = "API pour la gestion des contrats de travail")
public class ContractController {

    private final ContractService contractService;

    @Operation(summary = "Récupérer tous les contrats", description = "Retourne une liste paginée de tous les contrats")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contrats récupérés avec succès"),
            @ApiResponse(responseCode = "401", description = "Non autorisé", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<ContractDTO>> getAllContracts(
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        return ResponseEntity.ok(contractService.getAllContracts(pageable));
    }

    @Operation(summary = "Récupérer un contrat par ID", description = "Retourne un contrat spécifique par son identifiant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contrat trouvé"),
            @ApiResponse(responseCode = "404", description = "Contrat non trouvé", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContractDTO> getContractById(
            @Parameter(description = "ID du contrat") @PathVariable Long id) {
        return ResponseEntity.ok(contractService.getContractById(id));
    }

    @Operation(summary = "Créer un nouveau contrat", description = "Crée un nouveau contrat dans le système")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Contrat créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content),
            @ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ContractDTO> createContract(
            @RequestBody ContractCreateDTO createDTO) {
        ContractDTO created = contractService.createContract(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Mettre à jour un contrat", description = "Met à jour un contrat existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contrat mis à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content),
            @ApiResponse(responseCode = "404", description = "Contrat non trouvé", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ContractDTO> updateContract(
            @Parameter(description = "ID du contrat") @PathVariable Long id,
            @RequestBody ContractUpdateDTO updateDTO) {
        return ResponseEntity.ok(contractService.updateContract(id, updateDTO));
    }

    @Operation(summary = "Mettre à jour le statut d'un contrat", description = "Change le statut d'un contrat existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut du contrat mis à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide ou transition de statut non autorisée", content = @Content),
            @ApiResponse(responseCode = "404", description = "Contrat non trouvé", content = @Content)
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ContractDTO> updateContractStatus(
            @Parameter(description = "ID du contrat") @PathVariable Long id,
            @RequestBody ContractStatusUpdateDTO statusUpdateDTO) {
        return ResponseEntity.ok(contractService.updateContractStatus(id, statusUpdateDTO));
    }

    @Operation(summary = "Supprimer un contrat", description = "Supprime un contrat du système")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Contrat supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Contrat non trouvé", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContract(
            @Parameter(description = "ID du contrat") @PathVariable Long id) {
        contractService.deleteContract(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Récupérer les contrats d'un employé", description = "Retourne tous les contrats associés à un employé")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contrats récupérés avec succès"),
            @ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content)
    })
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<ContractDTO>> getEmployeeContracts(
            @Parameter(description = "ID de l'employé") @PathVariable Long employeeId) {
        return ResponseEntity.ok(contractService.getEmployeeContracts(employeeId));
    }

    @Operation(summary = "Récupérer le contrat actif d'un employé", description = "Retourne le contrat actuellement actif d'un employé")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contrat actif récupéré avec succès"),
            @ApiResponse(responseCode = "204", description = "Aucun contrat actif trouvé"),
            @ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content)
    })
    @GetMapping("/employee/{employeeId}/active")
    public ResponseEntity<ContractDTO> getActiveEmployeeContract(
            @Parameter(description = "ID de l'employé") @PathVariable Long employeeId) {
        ContractDTO contract = contractService.getActiveEmployeeContract(employeeId);
        if (contract == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(contract);
    }

    @Operation(summary = "Récupérer les contrats par type", description = "Retourne tous les contrats d'un type spécifique")
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ContractDTO>> getContractsByType(
            @Parameter(description = "Type de contrat") @PathVariable Type type) {
        return ResponseEntity.ok(contractService.getContractsByType(type));
    }

    @Operation(summary = "Récupérer les contrats par statut", description = "Retourne tous les contrats ayant un statut spécifique")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ContractDTO>> getContractsByStatus(
            @Parameter(description = "Statut du contrat") @PathVariable ContratStatus status) {
        return ResponseEntity.ok(contractService.getContractsByStatus(status));
    }

    @Operation(summary = "Récupérer les contrats qui expirent bientôt", description = "Retourne les contrats qui expirent dans les prochains jours")
    @GetMapping("/expiring")
    public ResponseEntity<List<ContractDTO>> getExpiringContracts(
            @Parameter(description = "Nombre de jours avant expiration") @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(contractService.getExpiringContracts(days));
    }

    @Operation(summary = "Générer un document de contrat", description = "Génère un document Word basé sur un contrat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document généré avec succès"),
            @ApiResponse(responseCode = "404", description = "Contrat non trouvé", content = @Content)
    })
    @GetMapping("/{id}/document")
    public ResponseEntity<Resource> generateContractDocument(
            @Parameter(description = "ID du contrat") @PathVariable Long id) {

        ContractDTO contract = contractService.getContractById(id);
        byte[] document = contractService.generateContractDocument(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Contrat_" + contract.getEmployee().getLastName() + "_" + contract.getType() + ".docx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(new ByteArrayResource(document));
    }

    @Operation(summary = "Rechercher des contrats", description = "Recherche des contrats selon plusieurs critères")
    @GetMapping("/search")
    public ResponseEntity<Page<ContractDTO>> searchContracts(
            @Parameter(description = "Mot-clé pour la recherche dans le nom de l'employé")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Type de contrat")
            @RequestParam(required = false) Type type,

            @Parameter(description = "Statut du contrat")
            @RequestParam(required = false) ContratStatus status,

            @Parameter(description = "Date de début minimum (format yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateMin,

            @Parameter(description = "Date de début maximum (format yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateMax,

            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {

        return ResponseEntity.ok(contractService.searchContracts(
                keyword, type, status, startDateMin, startDateMax, pageable));
    }
}