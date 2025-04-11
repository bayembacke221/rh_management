package sn.bmbacke.rh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.bmbacke.rh.entity.enums.LeaveType;
import sn.bmbacke.rh.payload.dto.LeavePolicyCreateDTO;
import sn.bmbacke.rh.payload.dto.LeavePolicyDTO;
import sn.bmbacke.rh.service.LeavePolicyService;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des politiques de congés
 */
@RestController
@RequestMapping("leave-policies")
@RequiredArgsConstructor
@Tag(name = "Politiques de congés", description = "API pour la gestion des politiques de congés")
public class LeavePolicyController {

    private final LeavePolicyService leavePolicyService;

    @GetMapping
    @Operation(summary = "Récupérer toutes les politiques de congés", description = "Retourne une liste de toutes les politiques de congés")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Politiques de congés récupérées avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    public ResponseEntity<List<LeavePolicyDTO>> getAllLeavePolicies() {
        return ResponseEntity.ok(leavePolicyService.getAllLeavePolicies());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une politique de congés par ID", description = "Retourne une politique de congés spécifique par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Politique de congés trouvée"),
            @ApiResponse(responseCode = "404", description = "Politique de congés non trouvée", content = @Content)
    })
    public ResponseEntity<LeavePolicyDTO> getLeavePolicyById(
            @Parameter(description = "ID de la politique de congés") @PathVariable Long id) {
        return ResponseEntity.ok(leavePolicyService.getLeavePolicyById(id));
    }

    @GetMapping("/type/{leaveType}/year/{year}")
    @Operation(summary = "Récupérer une politique de congés par type et année", description = "Retourne une politique de congés pour un type et une année spécifiques")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Politique de congés trouvée"),
            @ApiResponse(responseCode = "404", description = "Politique de congés non trouvée", content = @Content)
    })
    public ResponseEntity<LeavePolicyDTO> getLeavePolicyByTypeAndYear(
            @Parameter(description = "Type de congés") @PathVariable LeaveType leaveType,
            @Parameter(description = "Année") @PathVariable Integer year) {
        LeavePolicyDTO policy = leavePolicyService.getLeavePolicyByTypeAndYear(leaveType, year);
        if (policy == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(policy);
    }

    @GetMapping("/year/{year}")
    @Operation(summary = "Récupérer les politiques de congés pour une année donnée", description = "Retourne les politiques de congés pour une année spécifique")
    public ResponseEntity<List<LeavePolicyDTO>> getLeavePoliciesByYear(
            @Parameter(description = "Année") @PathVariable Integer year) {
        return ResponseEntity.ok(leavePolicyService.getLeavePoliciesByYear(year));
    }

    @PostMapping
    @Operation(summary = "Créer une nouvelle politique de congés", description = "Crée une nouvelle politique de congés et retourne la politique créée")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Politique de congés créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    public ResponseEntity<LeavePolicyDTO> createLeavePolicy(
            @RequestBody LeavePolicyCreateDTO leavePolicyCreateDTO) {
        return new ResponseEntity<>(leavePolicyService.createLeavePolicy(leavePolicyCreateDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une politique de congés", description = "Met à jour une politique de congés existante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Politique de congés mise à jour avec succès"),
            @ApiResponse(responseCode = "404", description = "Politique de congés non trouvée", content = @Content),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    public ResponseEntity<LeavePolicyDTO> updateLeavePolicy(
            @Parameter(description = "ID de la politique de congés") @PathVariable Long id,
            @RequestBody LeavePolicyCreateDTO leavePolicyUpdateDTO) {
        return ResponseEntity.ok(leavePolicyService.updateLeavePolicy(id, leavePolicyUpdateDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une politique de congés", description = "Supprime une politique de congés existante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Politique de congés supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Politique de congés non trouvée", content = @Content)
    })
    public ResponseEntity<Void> deleteLeavePolicy(
            @Parameter(description = "ID de la politique de congés") @PathVariable Long id) {
        leavePolicyService.deleteLeavePolicy(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/duplicate")
    @Operation(summary = "Dupliquer des politiques de congés", description = "Duplique des politiques de congés d'une année à une autre")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Politiques dupliquées avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    public ResponseEntity<List<LeavePolicyDTO>> duplicateLeavePolicies(
            @Parameter(description = "Année source") @RequestParam Integer sourceYear,
            @Parameter(description = "Année cible") @RequestParam Integer targetYear) {
        return ResponseEntity.ok(leavePolicyService.duplicateLeavePolicies(sourceYear, targetYear));
    }
}
