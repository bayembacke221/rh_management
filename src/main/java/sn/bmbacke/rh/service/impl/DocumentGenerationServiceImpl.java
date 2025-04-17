package sn.bmbacke.rh.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import sn.bmbacke.rh.entity.enums.Status;
import sn.bmbacke.rh.entity.enums.Type;
import sn.bmbacke.rh.exception.ResourceNotFoundException;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.service.DocumentGenerationService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Implémentation du service de génération de documents (PDF, Excel, CSV)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentGenerationServiceImpl implements DocumentGenerationService {

    @Override
    public byte[] generatePDF(String templateName, Map<String, Object> data) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // Créer un nouveau document PDF
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Ajouter les contenus selon le type de template
            switch (templateName) {
                case "turnoverTemplate":
                    addTurnoverReportContent(document, data);
                    break;
                case "absenteeismTemplate":
                    addAbsenteeismReportContent(document, data);
                    break;
                case "salaryTemplate":
                    addSalaryReportContent(document, data);
                    break;
                case "distributionTemplate":
                    addDistributionReportContent(document, data);
                    break;
                default:
                    addDefaultReportContent(document, data);
            }

            document.close();
        } catch (DocumentException e) {
            log.error("Erreur lors de la génération du PDF", e);
            throw new ResourceNotFoundException("Erreur lors de la génération du PDF: " + e.getMessage());
        }

        return outputStream.toByteArray();
    }

    @Override
    public byte[] generateExcel(String sheetName, Object data) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Créer une nouvelle feuille Excel
            Sheet sheet = workbook.createSheet(sheetName);

            // Ajouter les contenus selon le type de données
            if (data instanceof TurnoverReportDTO) {
                addTurnoverReportToExcel(workbook, sheet, (TurnoverReportDTO) data);
            } else if (data instanceof AbsenteeismReportDTO) {
                addAbsenteeismReportToExcel(workbook, sheet, (AbsenteeismReportDTO) data);
            } else if (data instanceof SalaryCostReportDTO) {
                addSalaryReportToExcel(workbook, sheet, (SalaryCostReportDTO) data);
            } else if (data instanceof EmployeeDistributionReportDTO) {
                addDistributionReportToExcel(workbook, sheet, (EmployeeDistributionReportDTO) data);
            } else {
                addGenericDataToExcel(workbook, sheet, data);
            }

            // Ajuster la largeur des colonnes
            for (int i = 0; i < 20; i++) {
                sheet.autoSizeColumn(i);
            }

            // Écrire le workbook dans l'output stream
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Erreur lors de la génération de l'Excel", e);
            throw new RuntimeException("Erreur lors de la génération de l'Excel: " + e.getMessage());
        }
    }

    @Override
    public byte[] generateCSV(Object data) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // Convertir l'objet en lignes CSV
            List<String> lines = convertObjectToCSVLines(data);

            // Joindre les lignes avec des retours à la ligne
            String csvContent = String.join("\n", lines);

            // Écrire le contenu CSV dans l'output stream
            outputStream.write(csvContent.getBytes());

            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Erreur lors de la génération du CSV", e);
            throw new RuntimeException("Erreur lors de la génération du CSV: " + e.getMessage());
        }
    }

    /**
     * Ajoute le contenu d'un rapport de turnover dans un document PDF
     */
    private void addTurnoverReportContent(Document document, Map<String, Object> data) throws DocumentException {
        TurnoverReportDTO report = (TurnoverReportDTO) data.get("report");

        // Titre
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Rapport de Turnover - " + report.getYear(), titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Date de génération
        Font dateFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 12);
        Paragraph dateGeneration = new Paragraph("Généré le: " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), dateFont);
        dateGeneration.setAlignment(Paragraph.ALIGN_RIGHT);
        dateGeneration.setSpacingAfter(20);
        document.add(dateGeneration);

        // Résumé
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        document.add(new Paragraph("Résumé:", boldFont));
        document.add(new Paragraph("Nombre d'employés au début de l'année: " + report.getStartEmployeeCount(), normalFont));
        document.add(new Paragraph("Nombre d'employés à la fin de l'année: " + report.getEndEmployeeCount(), normalFont));
        document.add(new Paragraph("Nouveaux employés: " + report.getNewHires(), normalFont));
        document.add(new Paragraph("Départs: " + report.getDepartures(), normalFont));
        document.add(new Paragraph("Taux de turnover: " + report.getTurnoverRate() + "%", normalFont));
        document.add(new Paragraph(" ", normalFont)); // Espace

        // Turnover mensuel
        document.add(new Paragraph("Turnover mensuel:", boldFont));
        PdfPTable monthlyTable = new PdfPTable(4);
        monthlyTable.setWidthPercentage(100);

        // En-têtes
        addTableHeader(monthlyTable, Arrays.asList("Mois", "Nouveaux employés", "Départs", "Taux (%)"));

        // Données
        for (MonthlyTurnoverDTO monthly : report.getMonthlyTurnover()) {
            monthlyTable.addCell(monthly.getMonthName());
            monthlyTable.addCell(String.valueOf(monthly.getNewHires()));
            monthlyTable.addCell(String.valueOf(monthly.getDepartures()));
            monthlyTable.addCell(String.valueOf(monthly.getTurnoverRate()));
        }

        document.add(monthlyTable);
        document.add(new Paragraph(" ", normalFont)); // Espace

        // Turnover par département
        document.add(new Paragraph("Turnover par département:", boldFont));
        PdfPTable deptTable = new PdfPTable(6);
        deptTable.setWidthPercentage(100);

        // En-têtes
        addTableHeader(deptTable, Arrays.asList("Département", "Début", "Fin", "Nouveaux", "Départs", "Taux (%)"));

        // Données
        for (DepartmentTurnoverDTO dept : report.getDepartmentTurnover()) {
            deptTable.addCell(dept.getDepartmentName());
            deptTable.addCell(String.valueOf(dept.getStartEmployeeCount()));
            deptTable.addCell(String.valueOf(dept.getEndEmployeeCount()));
            deptTable.addCell(String.valueOf(dept.getNewHires()));
            deptTable.addCell(String.valueOf(dept.getDepartures()));
            deptTable.addCell(String.valueOf(dept.getTurnoverRate()));
        }

        document.add(deptTable);
    }


    /**
     * Ajoute les en-têtes à une table PDF
     */
    private void addTableHeader(PdfPTable table, List<String> headers) {
        headers.forEach(headerTitle -> {
            PdfPCell header = new PdfPCell();
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
            header.setPhrase(new Phrase(headerTitle, headerFont));
            header.setBackgroundColor(new BaseColor(220, 220, 220));
            table.addCell(header);
        });
    }

    /**
     * Ajoute les données d'un rapport de turnover dans une feuille Excel
     */
    private void addTurnoverReportToExcel(Workbook workbook, Sheet sheet, TurnoverReportDTO report) {
        // Créer les styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int rowNum = 0;
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Rapport de Turnover - " + report.getYear());

        rowNum++; // Ligne vide

        // Résumé
        Row summaryTitleRow = sheet.createRow(rowNum++);
        summaryTitleRow.createCell(0).setCellValue("Résumé:");

        Row startCountRow = sheet.createRow(rowNum++);
        startCountRow.createCell(0).setCellValue("Nombre d'employés au début de l'année:");
        startCountRow.createCell(1).setCellValue(report.getStartEmployeeCount());

        Row endCountRow = sheet.createRow(rowNum++);
        endCountRow.createCell(0).setCellValue("Nombre d'employés à la fin de l'année:");
        endCountRow.createCell(1).setCellValue(report.getEndEmployeeCount());

        Row newHiresRow = sheet.createRow(rowNum++);
        newHiresRow.createCell(0).setCellValue("Nouveaux employés:");
        newHiresRow.createCell(1).setCellValue(report.getNewHires());

        Row departuresRow = sheet.createRow(rowNum++);
        departuresRow.createCell(0).setCellValue("Départs:");
        departuresRow.createCell(1).setCellValue(report.getDepartures());

        Row rateRow = sheet.createRow(rowNum++);
        rateRow.createCell(0).setCellValue("Taux de turnover:");
        rateRow.createCell(1).setCellValue(report.getTurnoverRate() + "%");

        rowNum++; // Ligne vide

        // Turnover mensuel
        Row monthlyTitleRow = sheet.createRow(rowNum++);
        monthlyTitleRow.createCell(0).setCellValue("Turnover mensuel:");

        Row monthlyHeaderRow = sheet.createRow(rowNum++);
        Cell monthHeader = monthlyHeaderRow.createCell(0);
        monthHeader.setCellValue("Mois");
        monthHeader.setCellStyle(headerStyle);

        Cell newHiresHeader = monthlyHeaderRow.createCell(1);
        newHiresHeader.setCellValue("Nouveaux employés");
        newHiresHeader.setCellStyle(headerStyle);

        Cell departuresHeader = monthlyHeaderRow.createCell(2);
        departuresHeader.setCellValue("Départs");
        departuresHeader.setCellStyle(headerStyle);

        Cell rateHeader = monthlyHeaderRow.createCell(3);
        rateHeader.setCellValue("Taux (%)");
        rateHeader.setCellStyle(headerStyle);

        for (MonthlyTurnoverDTO monthly : report.getMonthlyTurnover()) {
            Row dataRow = sheet.createRow(rowNum++);

            Cell monthCell = dataRow.createCell(0);
            monthCell.setCellValue(monthly.getMonthName());
            monthCell.setCellStyle(dataStyle);

            Cell newHiresCell = dataRow.createCell(1);
            newHiresCell.setCellValue(monthly.getNewHires());
            newHiresCell.setCellStyle(dataStyle);

            Cell departuresCell = dataRow.createCell(2);
            departuresCell.setCellValue(monthly.getDepartures());
            departuresCell.setCellStyle(dataStyle);

            Cell rateCell = dataRow.createCell(3);
            rateCell.setCellValue(monthly.getTurnoverRate());
            rateCell.setCellStyle(dataStyle);
        }

        rowNum++; // Ligne vide

        // Turnover par département
        Row deptTitleRow = sheet.createRow(rowNum++);
        deptTitleRow.createCell(0).setCellValue("Turnover par département:");

        Row deptHeaderRow = sheet.createRow(rowNum++);
        int colNum = 0;
        for (String header : Arrays.asList("Département", "Début", "Fin", "Nouveaux", "Départs", "Taux (%)")) {
            Cell headerCell = deptHeaderRow.createCell(colNum++);
            headerCell.setCellValue(header);
            headerCell.setCellStyle(headerStyle);
        }

        for (DepartmentTurnoverDTO dept : report.getDepartmentTurnover()) {
            Row dataRow = sheet.createRow(rowNum++);

            colNum = 0;
            Cell deptNameCell = dataRow.createCell(colNum++);
            deptNameCell.setCellValue(dept.getDepartmentName());
            deptNameCell.setCellStyle(dataStyle);

            Cell startCountCell = dataRow.createCell(colNum++);
            startCountCell.setCellValue(dept.getStartEmployeeCount());
            startCountCell.setCellStyle(dataStyle);

            Cell endCountCell = dataRow.createCell(colNum++);
            endCountCell.setCellValue(dept.getEndEmployeeCount());
            endCountCell.setCellStyle(dataStyle);

            Cell newHiresCell = dataRow.createCell(colNum++);
            newHiresCell.setCellValue(dept.getNewHires());
            newHiresCell.setCellStyle(dataStyle);

            Cell departuresCell = dataRow.createCell(colNum++);
            departuresCell.setCellValue(dept.getDepartures());
            departuresCell.setCellStyle(dataStyle);

            Cell rateCell = dataRow.createCell(colNum++);
            rateCell.setCellValue(dept.getTurnoverRate());
            rateCell.setCellStyle(dataStyle);
        }
    }

    /**
     * Crée un style pour les en-têtes de tableau Excel
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * Crée un style pour les données de tableau Excel
     */
    private CellStyle createDataStyle(Workbook workbook) {
        return workbook.createCellStyle();
    }

    /**
     * Convertit un objet en lignes CSV
     */
    private List<String> convertObjectToCSVLines(Object data) {
        List<String> lines = new ArrayList<>();

        if (data instanceof TurnoverReportDTO) {
            // Existing implementation for TurnoverReportDTO
            TurnoverReportDTO report = (TurnoverReportDTO) data;

            // En-tête et résumé
            lines.add("Rapport de Turnover - " + report.getYear());
            lines.add("");
            lines.add("Résumé");
            lines.add("Nombre d'employés au début de l'année," + report.getStartEmployeeCount());
            lines.add("Nombre d'employés à la fin de l'année," + report.getEndEmployeeCount());
            lines.add("Nouveaux employés," + report.getNewHires());
            lines.add("Départs," + report.getDepartures());
            lines.add("Taux de turnover (%)," + report.getTurnoverRate());
            lines.add("");

            // Turnover mensuel
            lines.add("Turnover mensuel");
            lines.add("Mois,Nouveaux employés,Départs,Taux (%)");

            for (MonthlyTurnoverDTO monthly : report.getMonthlyTurnover()) {
                lines.add(monthly.getMonthName() + "," +
                        monthly.getNewHires() + "," +
                        monthly.getDepartures() + "," +
                        monthly.getTurnoverRate());
            }

            lines.add("");

            // Turnover par département
            lines.add("Turnover par département");
            lines.add("Département,Début,Fin,Nouveaux,Départs,Taux (%)");

            for (DepartmentTurnoverDTO dept : report.getDepartmentTurnover()) {
                lines.add(dept.getDepartmentName() + "," +
                        dept.getStartEmployeeCount() + "," +
                        dept.getEndEmployeeCount() + "," +
                        dept.getNewHires() + "," +
                        dept.getDepartures() + "," +
                        dept.getTurnoverRate());
            }
        } else if (data instanceof AbsenteeismReportDTO) {
            AbsenteeismReportDTO report = (AbsenteeismReportDTO) data;

            // En-tête et résumé
            lines.add("Rapport d'Absentéisme - " + report.getYear());
            lines.add("");
            lines.add("Résumé");
            lines.add("Nombre total d'employés," + report.getTotalEmployees());
            lines.add("Nombre total de jours ouvrables," + report.getTotalWorkingDays());
            lines.add("Nombre total de jours d'absence," + report.getTotalAbsenceDays());
            lines.add("Taux d'absentéisme (%)," + report.getAbsenteeismRate());
            lines.add("");

            // Absentéisme mensuel
            lines.add("Absentéisme mensuel");
            lines.add("Mois,Employés,Jours ouvrables,Jours d'absence,Taux (%)");

            for (MonthlyAbsenteeismDTO monthly : report.getMonthlyAbsenteeism()) {
                lines.add(monthly.getMonthName() + "," +
                        monthly.getTotalEmployees() + "," +
                        monthly.getWorkingDays() + "," +
                        monthly.getAbsenceDays() + "," +
                        monthly.getAbsenteeismRate());
            }

            lines.add("");

            // Absentéisme par département
            lines.add("Absentéisme par département");
            lines.add("Département,Employés,Jours d'absence,Taux (%)");

            for (DepartmentAbsenteeismDTO dept : report.getDepartmentAbsenteeism()) {
                lines.add(dept.getDepartmentName() + "," +
                        dept.getTotalEmployees() + "," +
                        dept.getAbsenceDays() + "," +
                        dept.getAbsenteeismRate());
            }

            lines.add("");

            // Absentéisme par type de congé
            lines.add("Répartition par type de congé");
            lines.add("Type de congé,Jours d'absence,Pourcentage (%)");

            for (LeaveTypeAbsenteeismDTO type : report.getLeaveTypeAbsenteeism()) {
                lines.add(type.getLeaveTypeName() + "," +
                        type.getAbsenceDays() + "," +
                        type.getPercentageOfTotal());
            }

        } else if (data instanceof SalaryCostReportDTO) {
            SalaryCostReportDTO report = (SalaryCostReportDTO) data;

            // En-tête et résumé
            lines.add("Rapport des Coûts Salariaux - " + report.getYear());
            lines.add("");
            lines.add("Résumé des coûts salariaux");
            lines.add("Coût salarial total," + report.getTotalSalaryCost());
            lines.add("Salaire moyen," + report.getAverageSalary());
            lines.add("Salaire médian," + report.getMedianSalary());
            lines.add("Salaire minimum," + report.getMinSalary());
            lines.add("Salaire maximum," + report.getMaxSalary());
            lines.add("");

            // Coûts mensuels
            lines.add("Coûts salariaux mensuels");
            lines.add("Mois,Coût total (FCFA),Nombre d'employés,Salaire moyen (FCFA)");

            for (MonthlySalaryCostDTO monthly : report.getMonthlyCosts()) {
                lines.add(monthly.getMonthName() + "," +
                        monthly.getTotalCost() + "," +
                        monthly.getEmployeeCount() + "," +
                        monthly.getAverageSalary());
            }

            lines.add("");

            // Coûts par département
            lines.add("Coûts salariaux par département");
            lines.add("Département,Coût total (FCFA),Employés,Salaire moyen (FCFA),% du total");

            for (DepartmentSalaryCostDTO dept : report.getDepartmentCosts()) {
                lines.add(dept.getDepartmentName() + "," +
                        dept.getTotalCost() + "," +
                        dept.getEmployeeCount() + "," +
                        dept.getAverageSalary() + "," +
                        dept.getPercentageOfTotal());
            }

            lines.add("");

            // Coûts par type de contrat
            lines.add("Coûts par type de contrat");
            lines.add("Type de contrat,Coût total (FCFA)");

            for (Map.Entry<String, BigDecimal> entry : report.getCostByContractType().entrySet()) {
                lines.add(entry.getKey() + "," + entry.getValue());
            }

        } else if (data instanceof EmployeeDistributionReportDTO) {
            EmployeeDistributionReportDTO report = (EmployeeDistributionReportDTO) data;

            // En-tête et résumé
            lines.add("Rapport de Distribution des Employés");
            lines.add("");
            lines.add("Nombre total d'employés," + report.getTotalEmployees());
            lines.add("");

            // Distribution par statut
            lines.add("Distribution par statut");
            lines.add("Statut,Nombre d'employés");

            for (Map.Entry<Status, Integer> entry : report.getStatusDistribution().entrySet()) {
                lines.add(entry.getKey().name() + "," + entry.getValue());
            }

            lines.add("");

            // Distribution par type de contrat
            lines.add("Distribution par type de contrat");
            lines.add("Type de contrat,Nombre d'employés");

            for (Map.Entry<Type, Integer> entry : report.getContractTypeDistribution().entrySet()) {
                lines.add(entry.getKey().name() + "," + entry.getValue());
            }

            lines.add("");

            // Distribution par département
            lines.add("Distribution par département");
            lines.add("Département,Nombre d'employés");

            for (Map.Entry<String, Integer> entry : report.getDepartmentDistribution().entrySet()) {
                lines.add(entry.getKey() + "," + entry.getValue());
            }

            lines.add("");

            // Distribution par poste
            lines.add("Distribution par poste");
            lines.add("Poste,Nombre d'employés");

            for (Map.Entry<String, Integer> entry : report.getPositionDistribution().entrySet()) {
                lines.add(entry.getKey() + "," + entry.getValue());
            }

            lines.add("");

            // Distribution par genre
            lines.add("Distribution par genre");
            lines.add("Genre,Nombre,Pourcentage (%)");

            GenderDistributionDTO genderDist = report.getGenderDistribution();
            lines.add("Homme," + genderDist.getMaleCount() + "," + genderDist.getMalePercentage());
            lines.add("Femme," + genderDist.getFemaleCount() + "," + genderDist.getFemalePercentage());

            lines.add("");

            // Distribution par âge
            lines.add("Distribution par âge");
            lines.add("Âge moyen," + report.getAgeDistribution().getAverageAge() + " ans");
            lines.add("");
            lines.add("Tranche d'âge,Nombre d'employés");

            AgeDistributionDTO ageDist = report.getAgeDistribution();
            lines.add("Moins de 25 ans," + ageDist.getUnder25Count());
            lines.add("25-34 ans," + ageDist.getAge25to34Count());
            lines.add("35-44 ans," + ageDist.getAge35to44Count());
            lines.add("45-54 ans," + ageDist.getAge45to54Count());
            lines.add("55 ans et plus," + ageDist.getAge55plusCount());

            lines.add("");

            // Distribution par ancienneté
            lines.add("Distribution par ancienneté");
            lines.add("Ancienneté moyenne," + report.getSeniorityDistribution().getAverageSeniority() + " années");
            lines.add("");
            lines.add("Ancienneté,Nombre d'employés");

            SeniorityDistributionDTO seniorityDist = report.getSeniorityDistribution();
            lines.add("Moins d'un an," + seniorityDist.getLessThan1YearCount());
            lines.add("1-3 ans," + seniorityDist.getOneToThreeYearsCount());
            lines.add("3-5 ans," + seniorityDist.getThreeToFiveYearsCount());
            lines.add("5-10 ans," + seniorityDist.getFiveToTenYearsCount());
            lines.add("Plus de 10 ans," + seniorityDist.getMoreThanTenYearsCount());

        } else {
            // Traitement générique
            lines.add("Données non prises en charge pour l'export CSV");

            // Si possible, extraire quelques informations basiques
            lines.add("Type de données: " + data.getClass().getSimpleName());
            lines.add("Représentation: " + data.toString());
        }

        return lines;
    }


    /**
     * Ajoute le contenu d'un rapport d'absentéisme dans un document PDF
     */
    private void addAbsenteeismReportContent(Document document, Map<String, Object> data) throws DocumentException {
        AbsenteeismReportDTO report = (AbsenteeismReportDTO) data.get("report");

        // Titre
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Rapport d'Absentéisme - " + report.getYear(), titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Date de génération
        Font dateFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 12);
        Paragraph dateGeneration = new Paragraph("Généré le: " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), dateFont);
        dateGeneration.setAlignment(Paragraph.ALIGN_RIGHT);
        dateGeneration.setSpacingAfter(20);
        document.add(dateGeneration);

        // Résumé
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        document.add(new Paragraph("Résumé:", boldFont));
        document.add(new Paragraph("Nombre total d'employés: " + report.getTotalEmployees(), normalFont));
        document.add(new Paragraph("Nombre total de jours ouvrables: " + report.getTotalWorkingDays(), normalFont));
        document.add(new Paragraph("Nombre total de jours d'absence: " + report.getTotalAbsenceDays(), normalFont));
        document.add(new Paragraph("Taux d'absentéisme: " + report.getAbsenteeismRate() + "%", normalFont));
        document.add(new Paragraph(" ", normalFont)); // Espace

        // Absentéisme mensuel
        document.add(new Paragraph("Absentéisme mensuel:", boldFont));
        PdfPTable monthlyTable = new PdfPTable(5);
        monthlyTable.setWidthPercentage(100);

        // En-têtes
        addTableHeader(monthlyTable, Arrays.asList("Mois", "Employés", "Jours ouvrables", "Jours d'absence", "Taux (%)"));

        // Données
        for (MonthlyAbsenteeismDTO monthly : report.getMonthlyAbsenteeism()) {
            monthlyTable.addCell(monthly.getMonthName());
            monthlyTable.addCell(String.valueOf(monthly.getTotalEmployees()));
            monthlyTable.addCell(String.valueOf(monthly.getWorkingDays()));
            monthlyTable.addCell(String.valueOf(monthly.getAbsenceDays()));
            monthlyTable.addCell(String.valueOf(monthly.getAbsenteeismRate()));
        }

        document.add(monthlyTable);
        document.add(new Paragraph(" ", normalFont)); // Espace

        // Absentéisme par département
        document.add(new Paragraph("Absentéisme par département:", boldFont));
        PdfPTable deptTable = new PdfPTable(4);
        deptTable.setWidthPercentage(100);

        // En-têtes
        addTableHeader(deptTable, Arrays.asList("Département", "Employés", "Jours d'absence", "Taux (%)"));

        // Données
        for (DepartmentAbsenteeismDTO dept : report.getDepartmentAbsenteeism()) {
            deptTable.addCell(dept.getDepartmentName());
            deptTable.addCell(String.valueOf(dept.getTotalEmployees()));
            deptTable.addCell(String.valueOf(dept.getAbsenceDays()));
            deptTable.addCell(String.valueOf(dept.getAbsenteeismRate()));
        }

        document.add(deptTable);
        document.add(new Paragraph(" ", normalFont)); // Espace

        // Absentéisme par type de congé
        document.add(new Paragraph("Répartition par type de congé:", boldFont));
        PdfPTable typeTable = new PdfPTable(3);
        typeTable.setWidthPercentage(100);

        // En-têtes
        addTableHeader(typeTable, Arrays.asList("Type de congé", "Jours d'absence", "Pourcentage (%)"));

        // Données
        for (LeaveTypeAbsenteeismDTO type : report.getLeaveTypeAbsenteeism()) {
            typeTable.addCell(type.getLeaveTypeName());
            typeTable.addCell(String.valueOf(type.getAbsenceDays()));
            typeTable.addCell(String.valueOf(type.getPercentageOfTotal()));
        }

        document.add(typeTable);
    }

    /**
     * Ajoute le contenu d'un rapport de coûts salariaux dans un document PDF
     */
    private void addSalaryReportContent(Document document, Map<String, Object> data) throws DocumentException {
        SalaryCostReportDTO report = (SalaryCostReportDTO) data.get("report");

        // Titre
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Rapport des Coûts Salariaux - " + report.getYear(), titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Date de génération
        Font dateFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 12);
        Paragraph dateGeneration = new Paragraph("Généré le: " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), dateFont);
        dateGeneration.setAlignment(Paragraph.ALIGN_RIGHT);
        dateGeneration.setSpacingAfter(20);
        document.add(dateGeneration);

        // Résumé
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        document.add(new Paragraph("Résumé des coûts salariaux:", boldFont));
        document.add(new Paragraph("Coût salarial total: " + report.getTotalSalaryCost() + " FCFA", normalFont));
        document.add(new Paragraph("Salaire moyen: " + report.getAverageSalary() + " FCFA", normalFont));
        document.add(new Paragraph("Salaire médian: " + report.getMedianSalary() + " FCFA", normalFont));
        document.add(new Paragraph("Salaire minimum: " + report.getMinSalary() + " FCFA", normalFont));
        document.add(new Paragraph("Salaire maximum: " + report.getMaxSalary() + " FCFA", normalFont));
        document.add(new Paragraph(" ", normalFont)); // Espace

        // Coûts mensuels
        document.add(new Paragraph("Coûts salariaux mensuels:", boldFont));
        PdfPTable monthlyTable = new PdfPTable(4);
        monthlyTable.setWidthPercentage(100);

        // En-têtes
        addTableHeader(monthlyTable, Arrays.asList("Mois", "Coût total (FCFA)", "Nombre d'employés", "Salaire moyen (FCFA)"));

        // Données
        for (MonthlySalaryCostDTO monthly : report.getMonthlyCosts()) {
            monthlyTable.addCell(monthly.getMonthName());
            monthlyTable.addCell(String.valueOf(monthly.getTotalCost()));
            monthlyTable.addCell(String.valueOf(monthly.getEmployeeCount()));
            monthlyTable.addCell(String.valueOf(monthly.getAverageSalary()));
        }

        document.add(monthlyTable);
        document.add(new Paragraph(" ", normalFont)); // Espace

        // Coûts par département
        document.add(new Paragraph("Coûts salariaux par département:", boldFont));
        PdfPTable deptTable = new PdfPTable(5);
        deptTable.setWidthPercentage(100);

        // En-têtes
        addTableHeader(deptTable, Arrays.asList("Département", "Coût total (FCFA)", "Employés", "Salaire moyen (FCFA)", "% du total"));

        // Données
        for (DepartmentSalaryCostDTO dept : report.getDepartmentCosts()) {
            deptTable.addCell(dept.getDepartmentName());
            deptTable.addCell(String.valueOf(dept.getTotalCost()));
            deptTable.addCell(String.valueOf(dept.getEmployeeCount()));
            deptTable.addCell(String.valueOf(dept.getAverageSalary()));
            deptTable.addCell(String.valueOf(dept.getPercentageOfTotal()) + "%");
        }

        document.add(deptTable);
        document.add(new Paragraph(" ", normalFont)); // Espace

        // Coûts par type de contrat
        document.add(new Paragraph("Coûts par type de contrat:", boldFont));
        PdfPTable typeTable = new PdfPTable(2);
        typeTable.setWidthPercentage(100);

        // En-têtes
        addTableHeader(typeTable, Arrays.asList("Type de contrat", "Coût total (FCFA)"));

        // Données
        for (Map.Entry<String, BigDecimal> entry : report.getCostByContractType().entrySet()) {
            typeTable.addCell(entry.getKey());
            typeTable.addCell(String.valueOf(entry.getValue()));
        }

        document.add(typeTable);
    }

    /**
     * Ajoute le contenu d'un rapport de distribution dans un document PDF
     */
    private void addDistributionReportContent(Document document, Map<String, Object> data) throws DocumentException {
        EmployeeDistributionReportDTO report = (EmployeeDistributionReportDTO) data.get("report");

        // Titre
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Rapport de Distribution des Employés", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Date de génération
        Font dateFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 12);
        Paragraph dateGeneration = new Paragraph("Généré le: " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), dateFont);
        dateGeneration.setAlignment(Paragraph.ALIGN_RIGHT);
        dateGeneration.setSpacingAfter(20);
        document.add(dateGeneration);

        // Résumé
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        document.add(new Paragraph("Nombre total d'employés: " + report.getTotalEmployees(), boldFont));
        document.add(new Paragraph(" ", normalFont)); // Espace

        // Distribution par statut
        document.add(new Paragraph("Distribution par statut:", boldFont));
        PdfPTable statusTable = new PdfPTable(2);
        statusTable.setWidthPercentage(70);

        // En-têtes
        addTableHeader(statusTable, Arrays.asList("Statut", "Nombre d'employés"));

        // Données
        for (Map.Entry<Status, Integer> entry : report.getStatusDistribution().entrySet()) {
            statusTable.addCell(entry.getKey().name());
            statusTable.addCell(String.valueOf(entry.getValue()));
        }

        document.add(statusTable);
        document.add(new Paragraph(" ", normalFont)); // Espace

        // Distribution par type de contrat
        document.add(new Paragraph("Distribution par type de contrat:", boldFont));
        PdfPTable contractTable = new PdfPTable(2);
        contractTable.setWidthPercentage(70);

        // En-têtes
        addTableHeader(contractTable, Arrays.asList("Type de contrat", "Nombre d'employés"));

        // Données
        for (Map.Entry<Type, Integer> entry : report.getContractTypeDistribution().entrySet()) {
            contractTable.addCell(entry.getKey().name());
            contractTable.addCell(String.valueOf(entry.getValue()));
        }

        document.add(contractTable);
        document.add(new Paragraph(" ", normalFont)); // Espace

        // Distribution par département
        document.add(new Paragraph("Distribution par département:", boldFont));
        PdfPTable deptTable = new PdfPTable(2);
        deptTable.setWidthPercentage(70);

        // En-têtes
        addTableHeader(deptTable, Arrays.asList("Département", "Nombre d'employés"));

        // Données
        for (Map.Entry<String, Integer> entry : report.getDepartmentDistribution().entrySet()) {
            deptTable.addCell(entry.getKey());
            deptTable.addCell(String.valueOf(entry.getValue()));
        }

        document.add(deptTable);
        document.add(new Paragraph(" ", normalFont)); // Espace

        // Distribution par genre
        document.add(new Paragraph("Distribution par genre:", boldFont));
        PdfPTable genderTable = new PdfPTable(3);
        genderTable.setWidthPercentage(70);

        // En-têtes
        addTableHeader(genderTable, Arrays.asList("Genre", "Nombre", "Pourcentage (%)"));

        // Données
        GenderDistributionDTO genderDist = report.getGenderDistribution();
        genderTable.addCell("Homme");
        genderTable.addCell(String.valueOf(genderDist.getMaleCount()));
        genderTable.addCell(String.valueOf(genderDist.getMalePercentage()));

        genderTable.addCell("Femme");
        genderTable.addCell(String.valueOf(genderDist.getFemaleCount()));
        genderTable.addCell(String.valueOf(genderDist.getFemalePercentage()));

        document.add(genderTable);
        document.add(new Paragraph(" ", normalFont)); // Espace

        // Distribution par âge
        document.add(new Paragraph("Distribution par âge:", boldFont));
        document.add(new Paragraph("Âge moyen: " + report.getAgeDistribution().getAverageAge() + " ans", normalFont));

        PdfPTable ageTable = new PdfPTable(2);
        ageTable.setWidthPercentage(70);

        // En-têtes
        addTableHeader(ageTable, Arrays.asList("Tranche d'âge", "Nombre d'employés"));

        // Données
        AgeDistributionDTO ageDist = report.getAgeDistribution();
        ageTable.addCell("Moins de 25 ans");
        ageTable.addCell(String.valueOf(ageDist.getUnder25Count()));

        ageTable.addCell("25-34 ans");
        ageTable.addCell(String.valueOf(ageDist.getAge25to34Count()));

        ageTable.addCell("35-44 ans");
        ageTable.addCell(String.valueOf(ageDist.getAge35to44Count()));

        ageTable.addCell("45-54 ans");
        ageTable.addCell(String.valueOf(ageDist.getAge45to54Count()));

        ageTable.addCell("55 ans et plus");
        ageTable.addCell(String.valueOf(ageDist.getAge55plusCount()));

        document.add(ageTable);
        document.add(new Paragraph(" ", normalFont)); // Espace

        // Distribution par ancienneté
        document.add(new Paragraph("Distribution par ancienneté:", boldFont));
        document.add(new Paragraph("Ancienneté moyenne: " + report.getSeniorityDistribution().getAverageSeniority() + " années", normalFont));

        PdfPTable seniorityTable = new PdfPTable(2);
        seniorityTable.setWidthPercentage(70);

        // En-têtes
        addTableHeader(seniorityTable, Arrays.asList("Ancienneté", "Nombre d'employés"));

        // Données
        SeniorityDistributionDTO seniorityDist = report.getSeniorityDistribution();
        seniorityTable.addCell("Moins d'un an");
        seniorityTable.addCell(String.valueOf(seniorityDist.getLessThan1YearCount()));

        seniorityTable.addCell("1-3 ans");
        seniorityTable.addCell(String.valueOf(seniorityDist.getOneToThreeYearsCount()));

        seniorityTable.addCell("3-5 ans");
        seniorityTable.addCell(String.valueOf(seniorityDist.getThreeToFiveYearsCount()));

        seniorityTable.addCell("5-10 ans");
        seniorityTable.addCell(String.valueOf(seniorityDist.getFiveToTenYearsCount()));

        seniorityTable.addCell("Plus de 10 ans");
        seniorityTable.addCell(String.valueOf(seniorityDist.getMoreThanTenYearsCount()));

        document.add(seniorityTable);
    }

    /**
     * Ajoute un contenu par défaut dans un document PDF
     */
    private void addDefaultReportContent(Document document, Map<String, Object> data) throws DocumentException {
        // Titre
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Rapport RH", titleFont);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Date de génération
        Font dateFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 12);
        LocalDate generatedDate = (LocalDate) data.getOrDefault("generatedDate", LocalDate.now());
        Paragraph dateGeneration = new Paragraph("Généré le: " +
                generatedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), dateFont);
        dateGeneration.setAlignment(Paragraph.ALIGN_RIGHT);
        dateGeneration.setSpacingAfter(20);
        document.add(dateGeneration);

        // Contenu générique
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

        document.add(new Paragraph("Ce rapport contient des données génériques.", normalFont));
        document.add(new Paragraph("Pour des rapports spécifiques, veuillez utiliser les types de rapports dédiés:", boldFont));
        document.add(new Paragraph("- Rapport de turnover", normalFont));
        document.add(new Paragraph("- Rapport d'absentéisme", normalFont));
        document.add(new Paragraph("- Rapport des coûts salariaux", normalFont));
        document.add(new Paragraph("- Rapport de distribution des employés", normalFont));

        // Afficher les données si elles existent
        if (data.containsKey("report")) {
            document.add(new Paragraph(" ", normalFont)); // Espace
            document.add(new Paragraph("Données du rapport:", boldFont));

            Object reportData = data.get("report");
            if (reportData instanceof Map) {
                Map<?, ?> reportMap = (Map<?, ?>) reportData;
                for (Map.Entry<?, ?> entry : reportMap.entrySet()) {
                    document.add(new Paragraph(entry.getKey() + ": " + entry.getValue(), normalFont));
                }
            } else {
                document.add(new Paragraph(reportData.toString(), normalFont));
            }
        }
    }

    /**
     * Ajoute les données d'un rapport d'absentéisme dans une feuille Excel
     */
    private void addAbsenteeismReportToExcel(Workbook workbook, Sheet sheet, AbsenteeismReportDTO report) {
        // Créer les styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int rowNum = 0;
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Rapport d'Absentéisme - " + report.getYear());

        rowNum++; // Ligne vide

        // Résumé
        Row summaryTitleRow = sheet.createRow(rowNum++);
        summaryTitleRow.createCell(0).setCellValue("Résumé:");

        Row employeesRow = sheet.createRow(rowNum++);
        employeesRow.createCell(0).setCellValue("Nombre total d'employés:");
        employeesRow.createCell(1).setCellValue(report.getTotalEmployees());

        Row workingDaysRow = sheet.createRow(rowNum++);
        workingDaysRow.createCell(0).setCellValue("Nombre total de jours ouvrables:");
        workingDaysRow.createCell(1).setCellValue(report.getTotalWorkingDays());

        Row absenceDaysRow = sheet.createRow(rowNum++);
        absenceDaysRow.createCell(0).setCellValue("Nombre total de jours d'absence:");
        absenceDaysRow.createCell(1).setCellValue(report.getTotalAbsenceDays());

        Row rateRow = sheet.createRow(rowNum++);
        rateRow.createCell(0).setCellValue("Taux d'absentéisme:");
        rateRow.createCell(1).setCellValue(report.getAbsenteeismRate() + "%");

        rowNum++; // Ligne vide

        // Absentéisme mensuel
        Row monthlyTitleRow = sheet.createRow(rowNum++);
        monthlyTitleRow.createCell(0).setCellValue("Absentéisme mensuel:");

        Row monthlyHeaderRow = sheet.createRow(rowNum++);
        String[] monthlyHeaders = {"Mois", "Employés", "Jours ouvrables", "Jours d'absence", "Taux (%)"};
        for (int i = 0; i < monthlyHeaders.length; i++) {
            Cell headerCell = monthlyHeaderRow.createCell(i);
            headerCell.setCellValue(monthlyHeaders[i]);
            headerCell.setCellStyle(headerStyle);
        }

        for (MonthlyAbsenteeismDTO monthly : report.getMonthlyAbsenteeism()) {
            Row dataRow = sheet.createRow(rowNum++);

            Cell monthCell = dataRow.createCell(0);
            monthCell.setCellValue(monthly.getMonthName());
            monthCell.setCellStyle(dataStyle);

            Cell empCell = dataRow.createCell(1);
            empCell.setCellValue(monthly.getTotalEmployees());
            empCell.setCellStyle(dataStyle);

            Cell workingCell = dataRow.createCell(2);
            workingCell.setCellValue(monthly.getWorkingDays());
            workingCell.setCellStyle(dataStyle);

            Cell absenceCell = dataRow.createCell(3);
            absenceCell.setCellValue(monthly.getAbsenceDays());
            absenceCell.setCellStyle(dataStyle);

            Cell rateCell = dataRow.createCell(4);
            rateCell.setCellValue(monthly.getAbsenteeismRate());
            rateCell.setCellStyle(dataStyle);
        }

        rowNum++; // Ligne vide

        // Absentéisme par département
        Row deptTitleRow = sheet.createRow(rowNum++);
        deptTitleRow.createCell(0).setCellValue("Absentéisme par département:");

        Row deptHeaderRow = sheet.createRow(rowNum++);
        String[] deptHeaders = {"Département", "Employés", "Jours d'absence", "Taux (%)"};
        for (int i = 0; i < deptHeaders.length; i++) {
            Cell headerCell = deptHeaderRow.createCell(i);
            headerCell.setCellValue(deptHeaders[i]);
            headerCell.setCellStyle(headerStyle);
        }

        for (DepartmentAbsenteeismDTO dept : report.getDepartmentAbsenteeism()) {
            Row dataRow = sheet.createRow(rowNum++);

            Cell deptCell = dataRow.createCell(0);
            deptCell.setCellValue(dept.getDepartmentName());
            deptCell.setCellStyle(dataStyle);

            Cell empCell = dataRow.createCell(1);
            empCell.setCellValue(dept.getTotalEmployees());
            empCell.setCellStyle(dataStyle);

            Cell absenceCell = dataRow.createCell(2);
            absenceCell.setCellValue(dept.getAbsenceDays());
            absenceCell.setCellStyle(dataStyle);

            Cell rateCell = dataRow.createCell(3);
            rateCell.setCellValue(dept.getAbsenteeismRate());
            rateCell.setCellStyle(dataStyle);
        }

        rowNum++; // Ligne vide

        // Absentéisme par type de congé
        Row typeTitleRow = sheet.createRow(rowNum++);
        typeTitleRow.createCell(0).setCellValue("Répartition par type de congé:");

        Row typeHeaderRow = sheet.createRow(rowNum++);
        String[] typeHeaders = {"Type de congé", "Jours d'absence", "Pourcentage (%)"};
        for (int i = 0; i < typeHeaders.length; i++) {
            Cell headerCell = typeHeaderRow.createCell(i);
            headerCell.setCellValue(typeHeaders[i]);
            headerCell.setCellStyle(headerStyle);
        }

        for (LeaveTypeAbsenteeismDTO type : report.getLeaveTypeAbsenteeism()) {
            Row dataRow = sheet.createRow(rowNum++);

            Cell typeCell = dataRow.createCell(0);
            typeCell.setCellValue(type.getLeaveTypeName());
            typeCell.setCellStyle(dataStyle);

            Cell absenceCell = dataRow.createCell(1);
            absenceCell.setCellValue(type.getAbsenceDays());
            absenceCell.setCellStyle(dataStyle);

            Cell percentCell = dataRow.createCell(2);
            percentCell.setCellValue(type.getPercentageOfTotal());
            percentCell.setCellStyle(dataStyle);
        }
    }

    /**
     * Ajoute les données d'un rapport de coûts salariaux dans une feuille Excel
     */
    private void addSalaryReportToExcel(Workbook workbook, Sheet sheet, SalaryCostReportDTO report) {
        // Créer les styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int rowNum = 0;
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Rapport des Coûts Salariaux - " + report.getYear());

        rowNum++; // Ligne vide

        // Résumé
        Row summaryTitleRow = sheet.createRow(rowNum++);
        summaryTitleRow.createCell(0).setCellValue("Résumé des coûts salariaux:");

        Row totalCostRow = sheet.createRow(rowNum++);
        totalCostRow.createCell(0).setCellValue("Coût salarial total:");
        totalCostRow.createCell(1).setCellValue(report.getTotalSalaryCost().doubleValue() + " FCFA");

        Row avgSalaryRow = sheet.createRow(rowNum++);
        avgSalaryRow.createCell(0).setCellValue("Salaire moyen:");
        avgSalaryRow.createCell(1).setCellValue(report.getAverageSalary().doubleValue() + " FCFA");

        Row medianSalaryRow = sheet.createRow(rowNum++);
        medianSalaryRow.createCell(0).setCellValue("Salaire médian:");
        medianSalaryRow.createCell(1).setCellValue(report.getMedianSalary().doubleValue() + " FCFA");

        Row minSalaryRow = sheet.createRow(rowNum++);
        minSalaryRow.createCell(0).setCellValue("Salaire minimum:");
        minSalaryRow.createCell(1).setCellValue(report.getMinSalary().doubleValue() + " FCFA");

        Row maxSalaryRow = sheet.createRow(rowNum++);
        maxSalaryRow.createCell(0).setCellValue("Salaire maximum:");
        maxSalaryRow.createCell(1).setCellValue(report.getMaxSalary().doubleValue() + " FCFA");

        rowNum++; // Ligne vide

        // Coûts mensuels
        Row monthlyTitleRow = sheet.createRow(rowNum++);
        monthlyTitleRow.createCell(0).setCellValue("Coûts salariaux mensuels:");

        Row monthlyHeaderRow = sheet.createRow(rowNum++);
        String[] monthlyHeaders = {"Mois", "Coût total (FCFA)", "Nombre d'employés", "Salaire moyen (FCFA)"};
        for (int i = 0; i < monthlyHeaders.length; i++) {
            Cell headerCell = monthlyHeaderRow.createCell(i);
            headerCell.setCellValue(monthlyHeaders[i]);
            headerCell.setCellStyle(headerStyle);
        }

        for (MonthlySalaryCostDTO monthly : report.getMonthlyCosts()) {
            Row dataRow = sheet.createRow(rowNum++);

            Cell monthCell = dataRow.createCell(0);
            monthCell.setCellValue(monthly.getMonthName());
            monthCell.setCellStyle(dataStyle);

            Cell costCell = dataRow.createCell(1);
            costCell.setCellValue(monthly.getTotalCost().doubleValue());
            costCell.setCellStyle(dataStyle);

            Cell empCell = dataRow.createCell(2);
            empCell.setCellValue(monthly.getEmployeeCount());
            empCell.setCellStyle(dataStyle);

            Cell avgCell = dataRow.createCell(3);
            avgCell.setCellValue(monthly.getAverageSalary().doubleValue());
            avgCell.setCellStyle(dataStyle);
        }

        rowNum++; // Ligne vide

        // Coûts par département
        Row deptTitleRow = sheet.createRow(rowNum++);
        deptTitleRow.createCell(0).setCellValue("Coûts salariaux par département:");

        Row deptHeaderRow = sheet.createRow(rowNum++);
        String[] deptHeaders = {"Département", "Coût total (FCFA)", "Employés", "Salaire moyen (FCFA)", "% du total"};
        for (int i = 0; i < deptHeaders.length; i++) {
            Cell headerCell = deptHeaderRow.createCell(i);
            headerCell.setCellValue(deptHeaders[i]);
            headerCell.setCellStyle(headerStyle);
        }

        for (DepartmentSalaryCostDTO dept : report.getDepartmentCosts()) {
            Row dataRow = sheet.createRow(rowNum++);

            Cell deptCell = dataRow.createCell(0);
            deptCell.setCellValue(dept.getDepartmentName());
            deptCell.setCellStyle(dataStyle);

            Cell costCell = dataRow.createCell(1);
            costCell.setCellValue(dept.getTotalCost().doubleValue());
            costCell.setCellStyle(dataStyle);

            Cell empCell = dataRow.createCell(2);
            empCell.setCellValue(dept.getEmployeeCount());
            empCell.setCellStyle(dataStyle);

            Cell avgCell = dataRow.createCell(3);
            avgCell.setCellValue(dept.getAverageSalary().doubleValue());
            avgCell.setCellStyle(dataStyle);

            Cell percentCell = dataRow.createCell(4);
            percentCell.setCellValue(dept.getPercentageOfTotal());
            percentCell.setCellStyle(dataStyle);
        }

        rowNum++; // Ligne vide

        // Coûts par type de contrat
        Row typeTitleRow = sheet.createRow(rowNum++);
        typeTitleRow.createCell(0).setCellValue("Coûts par type de contrat:");

        Row typeHeaderRow = sheet.createRow(rowNum++);
        String[] typeHeaders = {"Type de contrat", "Coût total (FCFA)"};
        for (int i = 0; i < typeHeaders.length; i++) {
            Cell headerCell = typeHeaderRow.createCell(i);
            headerCell.setCellValue(typeHeaders[i]);
            headerCell.setCellStyle(headerStyle);
        }

        for (Map.Entry<String, BigDecimal> entry : report.getCostByContractType().entrySet()) {
            Row dataRow = sheet.createRow(rowNum++);

            Cell typeCell = dataRow.createCell(0);
            typeCell.setCellValue(entry.getKey());
            typeCell.setCellStyle(dataStyle);

            Cell costCell = dataRow.createCell(1);
            costCell.setCellValue(entry.getValue().doubleValue());
            costCell.setCellStyle(dataStyle);
        }
    }

    /**
     * Ajoute les données d'un rapport de distribution dans une feuille Excel
     */
    private void addDistributionReportToExcel(Workbook workbook, Sheet sheet, EmployeeDistributionReportDTO report) {
        // Créer les styles
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int rowNum = 0;
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Rapport de Distribution des Employés");

        rowNum++; // Ligne vide

        // Total employés
        Row totalRow = sheet.createRow(rowNum++);
        totalRow.createCell(0).setCellValue("Nombre total d'employés:");
        totalRow.createCell(1).setCellValue(report.getTotalEmployees());

        rowNum++; // Ligne vide

        // Distribution par statut
        Row statusTitleRow = sheet.createRow(rowNum++);
        statusTitleRow.createCell(0).setCellValue("Distribution par statut:");

        Row statusHeaderRow = sheet.createRow(rowNum++);
        String[] statusHeaders = {"Statut", "Nombre d'employés"};
        for (int i = 0; i < statusHeaders.length; i++) {
            Cell headerCell = statusHeaderRow.createCell(i);
            headerCell.setCellValue(statusHeaders[i]);
            headerCell.setCellStyle(headerStyle);
        }

        for (Map.Entry<Status, Integer> entry : report.getStatusDistribution().entrySet()) {
            Row dataRow = sheet.createRow(rowNum++);

            Cell statusCell = dataRow.createCell(0);
            statusCell.setCellValue(entry.getKey().name());
            statusCell.setCellStyle(dataStyle);

            Cell countCell = dataRow.createCell(1);
            countCell.setCellValue(entry.getValue());
            countCell.setCellStyle(dataStyle);
        }

        rowNum++; // Ligne vide

        // Distribution par type de contrat
        Row contractTitleRow = sheet.createRow(rowNum++);
        contractTitleRow.createCell(0).setCellValue("Distribution par type de contrat:");

        Row contractHeaderRow = sheet.createRow(rowNum++);
        String[] contractHeaders = {"Type de contrat", "Nombre d'employés"};
        for (int i = 0; i < contractHeaders.length; i++) {
            Cell headerCell = contractHeaderRow.createCell(i);
            headerCell.setCellValue(contractHeaders[i]);
            headerCell.setCellStyle(headerStyle);
        }

        for (Map.Entry<Type, Integer> entry : report.getContractTypeDistribution().entrySet()) {
            Row dataRow = sheet.createRow(rowNum++);

            Cell typeCell = dataRow.createCell(0);
            typeCell.setCellValue(entry.getKey().name());
            typeCell.setCellStyle(dataStyle);

            Cell countCell = dataRow.createCell(1);
            countCell.setCellValue(entry.getValue());
            countCell.setCellStyle(dataStyle);
        }

        rowNum++; // Ligne vide

        // Distribution par département
        Row deptTitleRow = sheet.createRow(rowNum++);
        deptTitleRow.createCell(0).setCellValue("Distribution par département:");

        Row deptHeaderRow = sheet.createRow(rowNum++);
        String[] deptHeaders = {"Département", "Nombre d'employés"};
        for (int i = 0; i < deptHeaders.length; i++) {
            Cell headerCell = deptHeaderRow.createCell(i);
            headerCell.setCellValue(deptHeaders[i]);
            headerCell.setCellStyle(headerStyle);
        }

        for (Map.Entry<String, Integer> entry : report.getDepartmentDistribution().entrySet()) {
            Row dataRow = sheet.createRow(rowNum++);

            Cell deptCell = dataRow.createCell(0);
            deptCell.setCellValue(entry.getKey());
            deptCell.setCellStyle(dataStyle);

            Cell countCell = dataRow.createCell(1);
            countCell.setCellValue(entry.getValue());
            countCell.setCellStyle(dataStyle);
        }

        rowNum++; // Ligne vide

        // Distribution par genre
        Row genderTitleRow = sheet.createRow(rowNum++);
        genderTitleRow.createCell(0).setCellValue("Distribution par genre:");

        Row genderHeaderRow = sheet.createRow(rowNum++);
        String[] genderHeaders = {"Genre", "Nombre", "Pourcentage (%)"};
        for (int i = 0; i < genderHeaders.length; i++) {
            Cell headerCell = genderHeaderRow.createCell(i);
            headerCell.setCellValue(genderHeaders[i]);
            headerCell.setCellStyle(headerStyle);
        }

        GenderDistributionDTO genderDist = report.getGenderDistribution();

        Row maleRow = sheet.createRow(rowNum++);
        maleRow.createCell(0).setCellValue("Homme");
        maleRow.createCell(1).setCellValue(genderDist.getMaleCount());
        maleRow.createCell(2).setCellValue(genderDist.getMalePercentage());

        Row femaleRow = sheet.createRow(rowNum++);
        femaleRow.createCell(0).setCellValue("Femme");
        femaleRow.createCell(1).setCellValue(genderDist.getFemaleCount());
        femaleRow.createCell(2).setCellValue(genderDist.getFemalePercentage());

        rowNum++; // Ligne vide

        // Distribution par âge
        Row ageTitleRow = sheet.createRow(rowNum++);
        ageTitleRow.createCell(0).setCellValue("Distribution par âge:");

        Row ageAvgRow = sheet.createRow(rowNum++);
        ageAvgRow.createCell(0).setCellValue("Âge moyen:");
        ageAvgRow.createCell(1).setCellValue(report.getAgeDistribution().getAverageAge() + " ans");

        Row ageHeaderRow = sheet.createRow(rowNum++);
        String[] ageHeaders = {"Tranche d'âge", "Nombre d'employés"};
        for (int i = 0; i < ageHeaders.length; i++) {
            Cell headerCell = ageHeaderRow.createCell(i);
            headerCell.setCellValue(ageHeaders[i]);
            headerCell.setCellStyle(headerStyle);
        }

        AgeDistributionDTO ageDist = report.getAgeDistribution();

        String[] ageGroups = {"Moins de 25 ans", "25-34 ans", "35-44 ans", "45-54 ans", "55 ans et plus"};
        int[] ageCounts = {
                ageDist.getUnder25Count(),
                ageDist.getAge25to34Count(),
                ageDist.getAge35to44Count(),
                ageDist.getAge45to54Count(),
                ageDist.getAge55plusCount()
        };

        for (int i = 0; i < ageGroups.length; i++) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(ageGroups[i]);
            dataRow.createCell(1).setCellValue(ageCounts[i]);
        }

        rowNum++; // Ligne vide

        // Distribution par ancienneté
        Row seniorityTitleRow = sheet.createRow(rowNum++);
        seniorityTitleRow.createCell(0).setCellValue("Distribution par ancienneté:");

        Row seniorityAvgRow = sheet.createRow(rowNum++);
        seniorityAvgRow.createCell(0).setCellValue("Ancienneté moyenne:");
        seniorityAvgRow.createCell(1).setCellValue(report.getSeniorityDistribution().getAverageSeniority() + " années");

        Row seniorityHeaderRow = sheet.createRow(rowNum++);
        String[] seniorityHeaders = {"Ancienneté", "Nombre d'employés"};
        for (int i = 0; i < seniorityHeaders.length; i++) {
            Cell headerCell = seniorityHeaderRow.createCell(i);
            headerCell.setCellValue(seniorityHeaders[i]);
            headerCell.setCellStyle(headerStyle);
        }

        SeniorityDistributionDTO seniorityDist = report.getSeniorityDistribution();

        String[] seniorityGroups = {"Moins d'un an", "1-3 ans", "3-5 ans", "5-10 ans", "Plus de 10 ans"};
        int[] seniorityCounts = {
                seniorityDist.getLessThan1YearCount(),
                seniorityDist.getOneToThreeYearsCount(),
                seniorityDist.getThreeToFiveYearsCount(),
                seniorityDist.getFiveToTenYearsCount(),
                seniorityDist.getMoreThanTenYearsCount()
        };

        for (int i = 0; i < seniorityGroups.length; i++) {
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.createCell(0).setCellValue(seniorityGroups[i]);
            dataRow.createCell(1).setCellValue(seniorityCounts[i]);
        }
    }

    /**
     * Ajoute des données génériques dans une feuille Excel
     */
    private void addGenericDataToExcel(Workbook workbook, Sheet sheet, Object data) {
        int rowNum = 0;

        // Titre
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Rapport de données génériques");

        rowNum++; // Ligne vide

        // Information générique
        Row infoRow = sheet.createRow(rowNum++);
        infoRow.createCell(0).setCellValue("Type de données:");
        infoRow.createCell(1).setCellValue(data.getClass().getSimpleName());

        rowNum++; // Ligne vide

        if (data instanceof Map) {
            // Pour les données de type Map
            Map<?, ?> dataMap = (Map<?, ?>) data;

            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Clé");
            headerRow.createCell(1).setCellValue("Valeur");

            for (Map.Entry<?, ?> entry : dataMap.entrySet()) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(entry.getKey().toString());

                if (entry.getValue() != null) {
                    dataRow.createCell(1).setCellValue(entry.getValue().toString());
                } else {
                    dataRow.createCell(1).setCellValue("(null)");
                }
            }
        } else if (data instanceof Collection) {
            // Pour les données de type Collection
            Collection<?> dataCollection = (Collection<?>) data;

            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue("Index");
            headerRow.createCell(1).setCellValue("Valeur");

            int index = 0;
            for (Object item : dataCollection) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(index++);

                if (item != null) {
                    dataRow.createCell(1).setCellValue(item.toString());
                } else {
                    dataRow.createCell(1).setCellValue("(null)");
                }
            }
        } else {
            // Pour les autres types de données
            try {
                // Essayer d'utiliser la réflexion pour extraire les champs
                Field[] fields = data.getClass().getDeclaredFields();

                Row headerRow = sheet.createRow(rowNum++);
                headerRow.createCell(0).setCellValue("Propriété");
                headerRow.createCell(1).setCellValue("Valeur");

                for (Field field : fields) {
                    field.setAccessible(true);

                    Row dataRow = sheet.createRow(rowNum++);
                    dataRow.createCell(0).setCellValue(field.getName());

                    try {
                        Object value = field.get(data);
                        if (value != null) {
                            if (value instanceof Date || value instanceof LocalDate || value instanceof LocalDateTime) {
                                dataRow.createCell(1).setCellValue(value.toString());
                            } else if (value instanceof Number) {
                                dataRow.createCell(1).setCellValue(((Number) value).doubleValue());
                            } else {
                                dataRow.createCell(1).setCellValue(value.toString());
                            }
                        } else {
                            dataRow.createCell(1).setCellValue("(null)");
                        }
                    } catch (IllegalAccessException e) {
                        dataRow.createCell(1).setCellValue("(erreur)");
                    }
                }
            } catch (Exception e) {
                // En cas d'erreur, afficher simplement la représentation en chaîne
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue("Contenu");
                dataRow.createCell(1).setCellValue(data.toString());
            }
        }
    }
}