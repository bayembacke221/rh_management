package sn.bmbacke.rh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.service.PositionService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
@Tag(name = "Positions", description = "API pour gérer les postes dans le système RH")
public class PositionController {

    private final PositionService positionService;

    @GetMapping
    @Operation(summary = "Récupérer toutes les positions", description = "Récupère la liste paginée de toutes les positions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Positions récupérées avec succès",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<PositionDTO>> getAllPositions(
            @Parameter(description = "Paramètres de pagination")
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(positionService.getAllPositions(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une position par ID", description = "Récupère les détails d'une position spécifique par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Position trouvée",
                    content = @Content(schema = @Schema(implementation = PositionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Position non trouvée")
    })
    public ResponseEntity<PositionDTO> getPositionById(
            @Parameter(description = "ID de la position", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(positionService.getPositionById(id));
    }

    @PostMapping
    @Operation(summary = "Créer une nouvelle position", description = "Crée une nouvelle position dans le système")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Position créée avec succès",
                    content = @Content(schema = @Schema(implementation = PositionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<PositionDTO> createPosition(
            @Parameter(description = "Détails de la position à créer", required = true)
            @Valid @RequestBody PositionCreateDTO positionCreateDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(positionService.createPosition(positionCreateDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une position", description = "Met à jour les détails d'une position existante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Position mise à jour avec succès",
                    content = @Content(schema = @Schema(implementation = PositionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Position non trouvée")
    })
    public ResponseEntity<PositionDTO> updatePosition(
            @Parameter(description = "ID de la position à mettre à jour", required = true)
            @PathVariable Long id,
            @Parameter(description = "Détails mis à jour de la position", required = true)
            @Valid @RequestBody PositionUpdateDTO positionUpdateDTO) {
        return ResponseEntity.ok(positionService.updatePosition(id, positionUpdateDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une position", description = "Supprime une position si elle n'est pas utilisée")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Position supprimée avec succès"),
            @ApiResponse(responseCode = "400", description = "La position ne peut pas être supprimée car elle est utilisée"),
            @ApiResponse(responseCode = "404", description = "Position non trouvée")
    })
    public ResponseEntity<Void> deletePosition(
            @Parameter(description = "ID de la position à supprimer", required = true)
            @PathVariable Long id) {
        positionService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Récupérer les positions par département", description = "Récupère toutes les positions associées à un département spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Positions récupérées avec succès",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "404", description = "Département non trouvé")
    })
    public ResponseEntity<List<PositionDTO>> getPositionsByDepartment(
            @Parameter(description = "ID du département", required = true)
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(positionService.getPositionsByDepartment(departmentId));
    }

    @GetMapping("/active")
    @Operation(summary = "Récupérer les positions actives", description = "Récupère toutes les positions marquées comme actives")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Positions actives récupérées avec succès",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    public ResponseEntity<List<PositionDTO>> getActivePositions() {
        return ResponseEntity.ok(positionService.getActivePositions());
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'une position", description = "Active ou désactive une position")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut mis à jour avec succès",
                    content = @Content(schema = @Schema(implementation = PositionDTO.class))),
            @ApiResponse(responseCode = "400", description = "La position ne peut pas être désactivée car elle est utilisée"),
            @ApiResponse(responseCode = "404", description = "Position non trouvée")
    })
    public ResponseEntity<PositionDTO> updatePositionStatus(
            @Parameter(description = "ID de la position", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nouveau statut (true = actif, false = inactif)", required = true)
            @RequestParam Boolean active) {
        return ResponseEntity.ok(positionService.updatePositionStatus(id, active));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des positions", description = "Recherche des positions selon différents critères")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recherche effectuée avec succès",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<PositionDTO>> searchPositions(
            @Parameter(description = "Mot-clé pour la recherche (titre ou description)")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Filtrer par statut actif/inactif")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Filtrer par département")
            @RequestParam(required = false) Long departmentId,
            @Parameter(description = "Paramètres de pagination")
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(positionService.searchPositions(keyword, active, departmentId, pageable));
    }

    @GetMapping("/salary-range")
    @Operation(summary = "Récupérer les positions par plage de salaire",
            description = "Récupère toutes les positions dont la plage salariale est comprise entre les valeurs spécifiées")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Positions récupérées avec succès",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "400", description = "Plage de salaire invalide")
    })
    public ResponseEntity<List<PositionDTO>> getPositionsBySalaryRange(
            @Parameter(description = "Salaire minimum", required = true)
            @RequestParam BigDecimal minSalary,
            @Parameter(description = "Salaire maximum", required = true)
            @RequestParam BigDecimal maxSalary) {
        return ResponseEntity.ok(positionService.getPositionsBySalaryRange(minSalary, maxSalary));
    }

    @GetMapping("/check-title")
    @Operation(summary = "Vérifier l'unicité d'un titre",
            description = "Vérifie si un titre de position existe déjà (utile pour la validation côté client)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vérification effectuée avec succès")
    })
    public ResponseEntity<Boolean> checkTitleExists(
            @Parameter(description = "Titre à vérifier", required = true)
            @RequestParam String title,
            @Parameter(description = "ID de la position à exclure de la vérification (pour les mises à jour)")
            @RequestParam(required = false, defaultValue = "0") Long excludeId) {
        return ResponseEntity.ok(positionService.titleExists(title, excludeId));
    }
}