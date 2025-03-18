package sn.bmbacke.rh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.service.DepartementService;

import java.util.List;

@RestController
@RequestMapping("departments")
@RequiredArgsConstructor
@Tag(name = "Départements", description = "API pour la gestion des départements")
public class DepartementController {

    private final DepartementService departementService;

    @Operation(summary = "Récupérer tous les départements", description = "Retourne une liste paginée de tous les départements")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Départements récupérés avec succès"),
            @ApiResponse(responseCode = "401", description = "Non autorisé", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<DepartementDTO>> getAllDepartements(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(departementService.getAllDepartements(pageable));
    }

    @Operation(summary = "Récupérer un département par ID", description = "Retourne un département spécifique par son identifiant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Département trouvé"),
            @ApiResponse(responseCode = "404", description = "Département non trouvé", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DepartementDTO> getDepartementById(
            @Parameter(description = "ID du département") @PathVariable Long id) {
        return ResponseEntity.ok(departementService.getDepartementById(id));
    }

    @Operation(summary = "Créer un nouveau département", description = "Crée un nouveau département dans le système")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Département créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    @PostMapping
    public ResponseEntity<DepartementDTO> createDepartement(
            @RequestBody DepartementCreateDTO createDTO) {
        DepartementDTO created = departementService.createDepartement(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Mettre à jour un département", description = "Met à jour un département existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Département mis à jour avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content),
            @ApiResponse(responseCode = "404", description = "Département non trouvé", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<DepartementDTO> updateDepartement(
            @Parameter(description = "ID du département") @PathVariable Long id,
            @RequestBody DepartementUpdateDTO updateDTO) {
        return ResponseEntity.ok(departementService.updateDepartement(id, updateDTO));
    }

    @Operation(summary = "Activer/désactiver un département", description = "Change le statut d'un département (actif/inactif)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut du département mis à jour avec succès"),
            @ApiResponse(responseCode = "404", description = "Département non trouvé", content = @Content)
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<DepartementDTO> updateDepartementStatus(
            @Parameter(description = "ID du département") @PathVariable Long id,
            @Parameter(description = "Statut actif (true/false)") @RequestParam Boolean active) {
        return ResponseEntity.ok(departementService.updateDepartementStatus(id, active));
    }

    @Operation(summary = "Supprimer un département", description = "Supprime un département du système")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Département supprimé avec succès"),
            @ApiResponse(responseCode = "400", description = "Impossible de supprimer (département avec employés/sous-départements)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Département non trouvé", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartement(
            @Parameter(description = "ID du département") @PathVariable Long id) {
        departementService.deleteDepartement(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Rechercher des départements", description = "Recherche des départements par nom ou code")
    @GetMapping("/search")
    public ResponseEntity<List<DepartementDTO>> searchDepartements(
            @Parameter(description = "Mot-clé pour la recherche") @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(departementService.searchDepartements(keyword));
    }

    @Operation(summary = "Récupérer les employés d'un département", description = "Retourne la liste des employés affectés à un département")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employés récupérés avec succès"),
            @ApiResponse(responseCode = "404", description = "Département non trouvé", content = @Content)
    })
    @GetMapping("/{id}/employees")
    public ResponseEntity<Page<EmployeeShortDTO>> getEmployeesByDepartement(
            @Parameter(description = "ID du département") @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(departementService.getEmployeesByDepartement(id, pageable));
    }

    @Operation(summary = "Récupérer l'organigramme complet", description = "Retourne la structure hiérarchique complète des départements")
    @GetMapping("/tree")
    public ResponseEntity<List<DepartementTreeDTO>> getDepartementTree() {
        return ResponseEntity.ok(departementService.getDepartementTree());
    }

    @Operation(summary = "Récupérer les départements de premier niveau", description = "Retourne les départements sans parent")
    @GetMapping("/top-level")
    public ResponseEntity<List<DepartementDTO>> getTopLevelDepartements() {
        return ResponseEntity.ok(departementService.getTopLevelDepartements());
    }

    @Operation(summary = "Récupérer les sous-départements", description = "Retourne les sous-départements d'un département parent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sous-départements récupérés avec succès"),
            @ApiResponse(responseCode = "404", description = "Département parent non trouvé", content = @Content)
    })
    @GetMapping("/{id}/sub-departments")
    public ResponseEntity<List<DepartementDTO>> getSubDepartements(
            @Parameter(description = "ID du département parent") @PathVariable Long id) {
        return ResponseEntity.ok(departementService.getSubDepartements(id));
    }
}