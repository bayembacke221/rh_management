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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.bmbacke.rh.entity.enums.LeaveStatus;
import sn.bmbacke.rh.entity.enums.LeaveType;
import sn.bmbacke.rh.entity.User;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.service.LeaveService;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des congés
 */
@RestController
@RequestMapping("leaves")
@RequiredArgsConstructor
@Tag(name = "Congés", description = "API pour la gestion des demandes de congés")
public class LeaveController {

    private final LeaveService leaveService;

    @GetMapping
    @Operation(summary = "Récupérer toutes les demandes de congés", description = "Retourne une liste paginée de toutes les demandes de congés")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Demandes de congés récupérées avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    public ResponseEntity<Page<LeaveDTO>> getAllLeaves(
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        return ResponseEntity.ok(leaveService.getAllLeaves(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une demande de congés par ID", description = "Retourne une demande de congés spécifique par son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Demande de congés trouvée"),
            @ApiResponse(responseCode = "404", description = "Demande de congés non trouvée", content = @Content)
    })
    public ResponseEntity<LeaveDTO> getLeaveById(
            @Parameter(description = "ID de la demande de congés") @PathVariable Long id) {
        return ResponseEntity.ok(leaveService.getLeaveById(id));
    }

    @PostMapping
    @Operation(summary = "Créer une nouvelle demande de congés", description = "Crée une nouvelle demande de congés et retourne la demande créée")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Demande de congés créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    public ResponseEntity<LeaveDTO> createLeave(
            @RequestBody LeaveCreateDTO leaveCreateDTO) {
        return new ResponseEntity<>(leaveService.createLeave(leaveCreateDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une demande de congés", description = "Met à jour une demande de congés existante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Demande de congés mise à jour avec succès"),
            @ApiResponse(responseCode = "404", description = "Demande de congés non trouvée", content = @Content),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    public ResponseEntity<LeaveDTO> updateLeave(
            @Parameter(description = "ID de la demande de congés") @PathVariable Long id,
            @RequestBody LeaveUpdateDTO leaveUpdateDTO) {
        return ResponseEntity.ok(leaveService.updateLeave(id, leaveUpdateDTO));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'une demande de congés", description = "Approuve ou rejette une demande de congés")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut mis à jour avec succès"),
            @ApiResponse(responseCode = "404", description = "Demande de congés non trouvée", content = @Content),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    public ResponseEntity<LeaveDTO> updateLeaveStatus(
            @Parameter(description = "ID de la demande de congés") @PathVariable Long id,
            @RequestBody LeaveApprovalDTO leaveApprovalDTO,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Long approverId = user.getEmployee().getId();
        return ResponseEntity.ok(leaveService.updateLeaveStatus(id, leaveApprovalDTO, approverId));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Annuler une demande de congés", description = "Annule une demande de congés existante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Demande de congés annulée avec succès"),
            @ApiResponse(responseCode = "404", description = "Demande de congés non trouvée", content = @Content),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    public ResponseEntity<LeaveDTO> cancelLeave(
            @Parameter(description = "ID de la demande de congés") @PathVariable Long id) {
        return ResponseEntity.ok(leaveService.cancelLeave(id));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Récupérer les demandes de congés d'un employé", description = "Retourne les demandes de congés d'un employé spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Demandes de congés récupérées avec succès"),
            @ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content)
    })
    public ResponseEntity<Page<LeaveDTO>> getEmployeeLeaves(
            @Parameter(description = "ID de l'employé") @PathVariable Long employeeId,
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        return ResponseEntity.ok(leaveService.getEmployeeLeaves(employeeId, pageable));
    }

    @GetMapping("/pending/manager/{managerId}")
    @Operation(summary = "Récupérer les demandes de congés en attente pour un manager", description = "Retourne les demandes de congés en attente d'approbation par un manager spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Demandes de congés récupérées avec succès"),
            @ApiResponse(responseCode = "404", description = "Manager non trouvé", content = @Content)
    })
    public ResponseEntity<List<LeaveDTO>> getPendingLeavesByManager(
            @Parameter(description = "ID du manager") @PathVariable Long managerId) {
        return ResponseEntity.ok(leaveService.getPendingLeavesByManager(managerId));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des demandes de congés", description = "Recherche des demandes de congés selon différents critères")
    public ResponseEntity<Page<LeaveDTO>> searchLeaves(
            @Parameter(description = "ID de l'employé") @RequestParam(required = false) Long employeeId,
            @Parameter(description = "Statut de la demande") @RequestParam(required = false) LeaveStatus status,
            @Parameter(description = "Type de congés") @RequestParam(required = false) LeaveType leaveType,
            @Parameter(description = "Date de début minimum") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateMin,
            @Parameter(description = "Date de début maximum") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateMax,
            @Parameter(description = "ID de l'approbateur") @RequestParam(required = false) Long approvedById,
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        return ResponseEntity.ok(leaveService.searchLeaves(
                employeeId, status, leaveType, startDateMin, startDateMax, approvedById, pageable));
    }

    @GetMapping("/period")
    @Operation(summary = "Récupérer les congés pour une période donnée", description = "Retourne les congés approuvés pour une période spécifique")
    public ResponseEntity<List<LeaveDTO>> getLeavesInPeriod(
            @Parameter(description = "Date de début de la période") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin de la période") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(leaveService.getLeavesInPeriod(startDate, endDate));
    }

    @GetMapping("/department/{departmentId}/period")
    @Operation(summary = "Récupérer les congés d'un département pour une période donnée", description = "Retourne les congés approuvés d'un département pour une période spécifique")
    public ResponseEntity<List<LeaveDTO>> getLeavesByDepartmentInPeriod(
            @Parameter(description = "ID du département") @PathVariable Long departmentId,
            @Parameter(description = "Date de début de la période") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin de la période") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(leaveService.getLeavesByDepartmentInPeriod(departmentId, startDate, endDate));
    }

    @GetMapping("/overlap")
    @Operation(summary = "Vérifier le chevauchement de congés", description = "Vérifie si un employé a des congés qui se chevauchent avec une période donnée")
    public ResponseEntity<Boolean> checkOverlappingLeave(
            @Parameter(description = "ID de l'employé") @RequestParam Long employeeId,
            @Parameter(description = "Date de début") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Date de fin") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "ID de la demande à exclure") @RequestParam(required = false) Long excludeLeaveId) {
        return ResponseEntity.ok(leaveService.hasOverlappingLeave(employeeId, startDate, endDate, excludeLeaveId));
    }

    @GetMapping("/employee/{employeeId}/summary")
    @Operation(summary = "Récupérer un résumé des congés pour un employé", description = "Retourne un résumé des congés d'un employé spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Résumé récupéré avec succès"),
            @ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content)
    })
    public ResponseEntity<LeaveEmployeeSummaryDTO> getEmployeeLeaveSummary(
            @Parameter(description = "ID de l'employé") @PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getEmployeeLeaveSummary(employeeId));
    }
}

