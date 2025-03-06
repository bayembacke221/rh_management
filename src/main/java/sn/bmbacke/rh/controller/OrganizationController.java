package sn.bmbacke.rh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.bmbacke.rh.payload.dto.DepartementTreeDTO;
import sn.bmbacke.rh.payload.dto.EmployeeDTO;
import sn.bmbacke.rh.payload.dto.EmployeeShortDTO;
import sn.bmbacke.rh.service.DepartementService;
import sn.bmbacke.rh.service.EmployeeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("organizations")
@RequiredArgsConstructor
@Tag(name = "Organisation", description = "API pour la gestion de l'organigramme et de la structure organisationnelle")
public class OrganizationController {

    private final DepartementService departementService;
    private final EmployeeService employeeService;

    @Operation(summary = "Récupérer l'organigramme complet", description = "Retourne la structure hiérarchique de l'organisation avec départements et sous-départements")
    @GetMapping("/chart")
    public ResponseEntity<List<DepartementTreeDTO>> getOrganizationChart() {
        return ResponseEntity.ok(departementService.getDepartementTree());
    }

    @Operation(summary = "Récupérer l'organigramme d'un département", description = "Retourne la structure hiérarchique à partir d'un département spécifique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organigramme récupéré avec succès"),
            @ApiResponse(responseCode = "404", description = "Département non trouvé", content = @Content)
    })
    @GetMapping("/chart/{departmentId}")
    public ResponseEntity<DepartementTreeDTO> getDepartmentChart(
            @Parameter(description = "ID du département") @PathVariable Long departmentId) {
        // Récupérer tout l'arbre et filtrer le département demandé
        // (implémentation simplifiée, vous pourriez créer une méthode dédiée dans le service)
        List<DepartementTreeDTO> tree = departementService.getDepartementTree();
        DepartementTreeDTO result = findDepartmentInTree(tree, departmentId);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Récupérer les subordonnés d'un manager", description = "Retourne la liste des employés qui rapportent directement à un manager")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subordonnés récupérés avec succès"),
            @ApiResponse(responseCode = "404", description = "Manager non trouvé", content = @Content)
    })
    @GetMapping("/employees/{employeeId}/subordinates")
    public ResponseEntity<List<EmployeeDTO>> getSubordinates(
            @Parameter(description = "ID du manager") @PathVariable Long employeeId) {
        return ResponseEntity.ok(employeeService.getSubordinates(employeeId));
    }

    @Operation(summary = "Récupérer le résumé de l'organisation", description = "Retourne des statistiques globales sur l'organisation")
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getOrganizationSummary() {
        Map<String, Object> summary = new HashMap<>();

        // Statistiques des employés par département
        summary.put("departmentStats", employeeService.getEmployeeStatsByDepartment());

        // Comptage des employés actifs
        summary.put("activeEmployeeCount", employeeService.getActiveEmployeeCount());

        // Nouveaux employés
        summary.put("recentHires", employeeService.getRecentHires());

        return ResponseEntity.ok(summary);
    }

    /**
     * Méthode utilitaire pour trouver un département spécifique dans l'arbre
     */
    private DepartementTreeDTO findDepartmentInTree(List<DepartementTreeDTO> tree, Long departmentId) {
        if (tree == null || tree.isEmpty()) {
            return null;
        }

        for (DepartementTreeDTO dept : tree) {
            if (dept.getId().equals(departmentId)) {
                return dept;
            }

            DepartementTreeDTO found = findDepartmentInTree(dept.getChildren(), departmentId);
            if (found != null) {
                return found;
            }
        }

        return null;
    }
}
