package sn.bmbacke.rh.controller;


import io.swagger.v3.oas.annotations.Operation;
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
import sn.bmbacke.rh.entity.enums.Status;
import sn.bmbacke.rh.service.EmployeeService;

import java.util.List;

@RestController
@RequestMapping("employees")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "API pour la gestion des employés")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @Operation(summary = "Récupérer tous les employés", description = "Retourne une liste paginée de tous les employés")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des employés récupérée avec succès"),
            @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    public ResponseEntity<Page<EmployeeDTO>> getAllEmployees(
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un employé par ID", description = "Retourne un employé basé sur son ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employé trouvé"),
            @ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content)
    })
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PostMapping
    @Operation(summary = "Créer un nouvel employé", description = "Crée un nouvel employé et retourne l'employé créé")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Employé créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    public ResponseEntity<EmployeeDTO> createEmployee(@Valid @RequestBody EmployeeCreateDTO employeeCreateDTO) {
        return new ResponseEntity<>(employeeService.createEmployee(employeeCreateDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un employé", description = "Met à jour un employé existant et retourne l'employé mis à jour")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employé mis à jour avec succès"),
            @ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content)
    })
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateDTO employeeUpdateDTO) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employeeUpdateDTO));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Mettre à jour le statut d'un employé",
            description = "Met à jour uniquement le statut d'un employé")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statut mis à jour avec succès"),
            @ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content)
    })
    public ResponseEntity<EmployeeDTO> updateEmployeeStatus(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeStatusUpdateDTO statusUpdateDTO) {
        return ResponseEntity.ok(employeeService.updateEmployeeStatus(id, statusUpdateDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un employé", description = "Désactive un employé (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Employé supprimé avec succès"),
            @ApiResponse(responseCode = "404", description = "Employé non trouvé", content = @Content)
    })
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Récupérer les employés d'un département",
            description = "Retourne une liste paginée des employés d'un département spécifique")
    public ResponseEntity<Page<EmployeeDTO>> getEmployeesByDepartment(
            @PathVariable Long departmentId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(employeeService.getEmployeesByDepartement(departmentId, pageable));
    }

    @GetMapping("/{id}/subordinates")
    @Operation(summary = "Récupérer les subordonnés d'un manager",
            description = "Retourne la liste des employés qui rapportent à un manager spécifique")
    public ResponseEntity<List<EmployeeDTO>> getSubordinates(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getSubordinates(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des employés",
            description = "Recherche des employés selon différents critères")
    public ResponseEntity<Page<EmployeeDTO>> searchEmployees(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) Status status,
            @RequestParam(name = "departmentId", required = false) Long departmentId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(employeeService.searchEmployees(keyword, status, departmentId, pageable));
    }

    @GetMapping("/recent")
    @Operation(summary = "Récupérer les employés récemment embauchés",
            description = "Retourne la liste des employés embauchés au cours des 30 derniers jours")
    public ResponseEntity<List<EmployeeDTO>> getRecentHires() {
        return ResponseEntity.ok(employeeService.getRecentHires());
    }

    @GetMapping("/stats")
    @Operation(summary = "Récupérer les statistiques des employés par département",
            description = "Retourne des statistiques agrégées sur les employés par département")
    public ResponseEntity<List<DepartmentEmployeeStatsDTO>> getEmployeeStatsByDepartment() {
        return ResponseEntity.ok(employeeService.getEmployeeStatsByDepartment());
    }

    @GetMapping("/count")
    @Operation(summary = "Récupérer le nombre total d'employés actifs")
    public ResponseEntity<Long> getActiveEmployeeCount() {
        return ResponseEntity.ok(employeeService.getActiveEmployeeCount());
    }
}
