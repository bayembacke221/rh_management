package sn.bmbacke.rh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.service.ReportService;

import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des rapports
 */
@RestController
@RequestMapping("reports")
@RequiredArgsConstructor
@Tag(name = "Rapports", description = "API pour la génération de rapports et statistiques RH")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/generate")
    @Operation(summary = "Générer un rapport", description = "Génère un rapport selon le type et les paramètres spécifiés")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rapport généré avec succès"),
            @ApiResponse(responseCode = "400", description = "Type de rapport ou format non pris en charge", content = @Content)
    })
    public ResponseEntity<byte[]> generateReport(
            @Parameter(description = "Type de rapport (turnover, absenteeism, salary, distribution)")
            @RequestParam String reportType,

            @Parameter(description = "Année pour le rapport (par défaut: année courante)")
            @RequestParam(required = false) Integer year,

            @Parameter(description = "Date de début pour les rapports sur une période spécifique")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Date de fin pour les rapports sur une période spécifique")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Date de référence pour les rapports à une date précise")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate,

            @Parameter(description = "Format du rapport (PDF, EXCEL, CSV)")
            @RequestParam(defaultValue = "PDF") String format) {

        // Construire les paramètres du rapport
        Map<String, Object> parameters = new HashMap<>();
        if (year != null) {
            parameters.put("year", year);
        }
        if (startDate != null) {
            parameters.put("startDate", startDate);
        }
        if (endDate != null) {
            parameters.put("endDate", endDate);
        }
        if (referenceDate != null) {
            parameters.put("referenceDate", referenceDate);
        }

        // Générer le rapport
        ReportDataDTO reportData = reportService.generateReport(reportType, parameters, format);

        // Déterminer le type de contenu approprié
        String contentType;
        switch (format.toUpperCase()) {
            case "PDF":
                contentType = MediaType.APPLICATION_PDF_VALUE;
                break;
            case "EXCEL":
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                break;
            case "CSV":
                contentType = "text/csv";
                break;
            default:
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        // Retourner la réponse avec l'en-tête approprié pour le téléchargement
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + reportData.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body((byte[]) reportData.getData());
    }

    @GetMapping("/turnover/{year}")
    @Operation(summary = "Rapport de turnover", description = "Génère un rapport de turnover pour une année spécifique")
    public ResponseEntity<TurnoverReportDTO> getTurnoverReport(
            @Parameter(description = "Année pour le rapport") @PathVariable Integer year) {
        return ResponseEntity.ok(reportService.generateTurnoverReport(year));
    }

    @GetMapping("/absenteeism/{year}")
    @Operation(summary = "Rapport d'absentéisme annuel", description = "Génère un rapport d'absentéisme pour une année spécifique")
    public ResponseEntity<AbsenteeismReportDTO> getAbsenteeismReport(
            @Parameter(description = "Année pour le rapport") @PathVariable Integer year) {
        return ResponseEntity.ok(reportService.generateAbsenteeismReport(year));
    }

    @GetMapping("/absenteeism/period")
    @Operation(summary = "Rapport d'absentéisme pour une période", description = "Génère un rapport d'absentéisme pour une période spécifique")
    public ResponseEntity<AbsenteeismReportDTO> getAbsenteeismReportForPeriod(
            @Parameter(description = "Date de début de la période")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Date de fin de la période")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.generateAbsenteeismReportForPeriod(startDate, endDate));
    }

    @GetMapping("/salary/{year}")
    @Operation(summary = "Rapport des coûts salariaux", description = "Génère un rapport des coûts salariaux pour une année spécifique")
    public ResponseEntity<SalaryCostReportDTO> getSalaryCostReport(
            @Parameter(description = "Année pour le rapport") @PathVariable Integer year) {
        return ResponseEntity.ok(reportService.generateSalaryCostReport(year));
    }

    @GetMapping("/distribution")
    @Operation(summary = "Rapport de distribution des employés actuel", description = "Génère un rapport sur la distribution actuelle des employés")
    public ResponseEntity<EmployeeDistributionReportDTO> getCurrentEmployeeDistributionReport() {
        return ResponseEntity.ok(reportService.generateCurrentEmployeeDistributionReport());
    }

    @GetMapping("/distribution/date")
    @Operation(summary = "Rapport de distribution des employés à une date", description = "Génère un rapport sur la distribution des employés à une date spécifique")
    public ResponseEntity<EmployeeDistributionReportDTO> getEmployeeDistributionReport(
            @Parameter(description = "Date de référence")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate) {
        return ResponseEntity.ok(reportService.generateEmployeeDistributionReport(referenceDate));
    }

    @GetMapping("/dashboard/summary")
    @Operation(summary = "Résumé pour le tableau de bord", description = "Fournit un résumé des indicateurs clés pour le tableau de bord")
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        // Créer un résumé des indicateurs clés
        Map<String, Object> dashboardSummary = new HashMap<>();

        // Année courante
        int currentYear = Year.now().getValue();

        // Récupérer les données de turnover
        TurnoverReportDTO turnoverReport = reportService.generateTurnoverReport(currentYear);
        dashboardSummary.put("turnover", Map.of(
                "rate", turnoverReport.getTurnoverRate(),
                "newHires", turnoverReport.getNewHires(),
                "departures", turnoverReport.getDepartures(),
                "employeeCount", turnoverReport.getEndEmployeeCount()
        ));

        // Récupérer les données d'absentéisme
        AbsenteeismReportDTO absenteeismReport = reportService.generateAbsenteeismReport(currentYear);
        dashboardSummary.put("absenteeism", Map.of(
                "rate", absenteeismReport.getAbsenteeismRate(),
                "totalAbsenceDays", absenteeismReport.getTotalAbsenceDays()
        ));

        // Récupérer les données de coûts salariaux
        SalaryCostReportDTO salaryCostReport = reportService.generateSalaryCostReport(currentYear);
        dashboardSummary.put("salaryCost", Map.of(
                "totalCost", salaryCostReport.getTotalSalaryCost(),
                "averageSalary", salaryCostReport.getAverageSalary()
        ));

        // Récupérer la distribution des employés
        EmployeeDistributionReportDTO distributionReport = reportService.generateCurrentEmployeeDistributionReport();
        dashboardSummary.put("employeeDistribution", Map.of(
                "totalEmployees", distributionReport.getTotalEmployees(),
                "genderDistribution", distributionReport.getGenderDistribution()
        ));

        return ResponseEntity.ok(dashboardSummary);
    }
}