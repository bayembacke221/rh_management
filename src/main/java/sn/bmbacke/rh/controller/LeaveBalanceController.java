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
import sn.bmbacke.rh.payload.dto.LeaveBalanceAdjustmentDTO;
import sn.bmbacke.rh.payload.dto.LeaveBalanceCreateDTO;
import sn.bmbacke.rh.payload.dto.LeaveBalanceDTO;
import sn.bmbacke.rh.service.LeaveBalanceService;

import java.util.List;
/**
 * Contrôleur REST pour la gestion des soldes de congés
 */
@RestController
@RequestMapping("leave-balances")
@RequiredArgsConstructor
@Tag(name = "Soldes de congés", description = "API pour la gestion des soldes de congés")
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    @GetMapping
    @Operation(summary = "Récupérer tous les soldes de congés", description = "Retourne une liste de tous les soldes de congés")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Soldes de congés récupérés avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    public ResponseEntity<List<LeaveBalanceDTO>> getAllLeaveBalances() {
        return ResponseEntity.ok(leaveBalanceService.getAllLeaveBalances());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un solde de congés par ID", description = "Retourne un solde de congés spécifique par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solde de congés trouvé"),
            @ApiResponse(responseCode = "404", description = "Solde de congés non trouvé", content = @Content)
    })
    public ResponseEntity<LeaveBalanceDTO> getLeaveBalanceById(
            @Parameter(description = "ID du solde de congés") @PathVariable Long id) {
        return ResponseEntity.ok(leaveBalanceService.getLeaveBalanceById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Récupérer les soldes de congés d'un employé", description = "Retourne les soldes de congés d'un employé spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Soldes de congés récupérés avec succès"),
            @ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content)
    })
    public ResponseEntity<List<LeaveBalanceDTO>> getEmployeeLeaveBalances(
            @Parameter(description = "ID de l'employé") @PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveBalanceService.getEmployeeLeaveBalances(employeeId));
    }

    @GetMapping("/employee/{employeeId}/year/{year}")
    @Operation(summary = "Récupérer les soldes de congés d'un employé pour une année donnée", description = "Retourne les soldes de congés d'un employé pour une année spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Soldes de congés récupérés avec succès"),
            @ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content)
    })
    public ResponseEntity<List<LeaveBalanceDTO>> getEmployeeLeaveBalancesByYear(
            @Parameter(description = "ID de l'employé") @PathVariable Long employeeId,
            @Parameter(description = "Année") @PathVariable Integer year) {
        return ResponseEntity.ok(leaveBalanceService.getEmployeeLeaveBalancesByYear(employeeId, year));
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau solde de congés", description = "Crée un nouveau solde de congés et retourne le solde créé")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solde de congés créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    public ResponseEntity<LeaveBalanceDTO> createLeaveBalance(
            @RequestBody LeaveBalanceCreateDTO leaveBalanceCreateDTO) {
        return new ResponseEntity<>(leaveBalanceService.createLeaveBalance(leaveBalanceCreateDTO), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/adjust")
    @Operation(summary = "Ajuster un solde de congés", description = "Ajuste un solde de congés existant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solde ajusté avec succès"),
            @ApiResponse(responseCode = "404", description = "Solde de congés non trouvé", content = @Content),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    public ResponseEntity<LeaveBalanceDTO> adjustLeaveBalance(
            @Parameter(description = "ID du solde de congés") @PathVariable Long id,
            @RequestBody LeaveBalanceAdjustmentDTO adjustmentDTO) {
        return ResponseEntity.ok(leaveBalanceService.adjustLeaveBalance(id, adjustmentDTO));
    }

    @PostMapping("/init-year/{year}")
    @Operation(summary = "Initialiser les soldes de congés pour une année", description = "Initialise les soldes de congés pour tous les employés pour une année donnée")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Soldes initialisés avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    public ResponseEntity<Void> initializeYearlyLeaveBalances(
            @Parameter(description = "Année") @PathVariable Integer year) {
        leaveBalanceService.initializeYearlyLeaveBalances(year);
        return ResponseEntity.ok().build();
    }
}
