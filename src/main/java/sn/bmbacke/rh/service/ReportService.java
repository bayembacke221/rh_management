package sn.bmbacke.rh.service;

import sn.bmbacke.rh.payload.dto.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Interface du service de rapports RH
 */
public interface ReportService {

    /**
     * Génère un rapport RH selon le type et les paramètres spécifiés
     */
    ReportDataDTO generateReport(String reportType, Map<String, Object> parameters, String format);

    /**
     * Génère un rapport de turnover pour une année donnée
     */
    TurnoverReportDTO generateTurnoverReport(Integer year);

    /**
     * Génère un rapport d'absentéisme pour une année donnée
     */
    AbsenteeismReportDTO generateAbsenteeismReport(Integer year);

    /**
     * Génère un rapport d'absentéisme pour une période donnée
     */
    AbsenteeismReportDTO generateAbsenteeismReportForPeriod(LocalDate startDate, LocalDate endDate);

    /**
     * Génère un rapport des coûts salariaux pour une année donnée
     */
    SalaryCostReportDTO generateSalaryCostReport(Integer year);

    /**
     * Génère un rapport de distribution des employés à une date donnée
     */
    EmployeeDistributionReportDTO generateEmployeeDistributionReport(LocalDate referenceDate);

    /**
     * Génère un rapport de distribution des employés à la date actuelle
     */
    EmployeeDistributionReportDTO generateCurrentEmployeeDistributionReport();

    /**
     * Exporte un rapport au format spécifié (PDF, Excel, CSV)
     */
    byte[] exportReport(Object reportData, String reportType, String format);
}

