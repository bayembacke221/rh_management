package sn.bmbacke.rh.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.bmbacke.rh.entity.*;
import sn.bmbacke.rh.entity.enums.Gender;
import sn.bmbacke.rh.entity.enums.LeaveStatus;
import sn.bmbacke.rh.entity.enums.LeaveType;
import sn.bmbacke.rh.entity.enums.Status;
import sn.bmbacke.rh.exception.BusinessException;
import sn.bmbacke.rh.payload.dto.*;
import sn.bmbacke.rh.repository.ContractRepository;
import sn.bmbacke.rh.repository.DepartmentRepository;
import sn.bmbacke.rh.repository.EmployeeRepository;
import sn.bmbacke.rh.repository.LeaveRepository;
import sn.bmbacke.rh.service.DocumentGenerationService;
import sn.bmbacke.rh.service.ReportService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
/**
 * Implémentation du service de rapports
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final ContractRepository contractRepository;
    private final DepartmentRepository departmentRepository;
    private final DocumentGenerationService documentGenerationService;

    @Override
    public ReportDataDTO generateReport(String reportType, Map<String, Object> parameters, String format) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }

        Object reportData;
        String filename;

        switch (reportType.toLowerCase()) {
            case "turnover":
                Integer turnoverYear = parameters.containsKey("year") ?
                        (Integer) parameters.get("year") : Year.now().getValue();
                reportData = generateTurnoverReport(turnoverYear);
                filename = "turnover_report_" + turnoverYear;
                break;

            case "absenteeism":
                if (parameters.containsKey("startDate") && parameters.containsKey("endDate")) {
                    LocalDate startDate = (LocalDate) parameters.get("startDate");
                    LocalDate endDate = (LocalDate) parameters.get("endDate");
                    reportData = generateAbsenteeismReportForPeriod(startDate, endDate);
                    filename = "absenteeism_report_" +
                            startDate.format(DateTimeFormatter.ISO_DATE) + "_" +
                            endDate.format(DateTimeFormatter.ISO_DATE);
                } else {
                    Integer absenteeismYear = parameters.containsKey("year") ?
                            (Integer) parameters.get("year") : Year.now().getValue();
                    reportData = generateAbsenteeismReport(absenteeismYear);
                    filename = "absenteeism_report_" + absenteeismYear;
                }
                break;

            case "salary":
                Integer salaryYear = parameters.containsKey("year") ?
                        (Integer) parameters.get("year") : Year.now().getValue();
                reportData = generateSalaryCostReport(salaryYear);
                filename = "salary_cost_report_" + salaryYear;
                break;

            case "distribution":
                if (parameters.containsKey("referenceDate")) {
                    LocalDate referenceDate = (LocalDate) parameters.get("referenceDate");
                    reportData = generateEmployeeDistributionReport(referenceDate);
                    filename = "employee_distribution_" +
                            referenceDate.format(DateTimeFormatter.ISO_DATE);
                } else {
                    reportData = generateCurrentEmployeeDistributionReport();
                    filename = "employee_distribution_current";
                }
                break;

            default:
                throw new BusinessException("Type de rapport non pris en charge : " + reportType);
        }

        byte[] reportContent = exportReport(reportData, reportType, format);

        ReportDataDTO reportDTO = new ReportDataDTO();
        reportDTO.setData(reportContent);
        reportDTO.setFormat(format);
        reportDTO.setFilename(filename + "." + format.toLowerCase());

        return reportDTO;
    }

    @Override
    public TurnoverReportDTO generateTurnoverReport(Integer year) {
        if (year == null) {
            year = Year.now().getValue();
        }

        // Définir les dates de début et de fin d'année
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        // Récupérer tous les employés qui étaient présents au début de l'année
        List<Employee> startEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getHireDate() != null && e.getHireDate().isBefore(startDate))
                .filter(e -> e.getEndDate() == null || e.getEndDate().isAfter(startDate))
                .collect(Collectors.toList());

        // Récupérer tous les employés qui étaient présents à la fin de l'année
        List<Employee> endEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getHireDate() != null && e.getHireDate().isBefore(endDate.plusDays(1)))
                .filter(e -> e.getEndDate() == null || e.getEndDate().isAfter(endDate))
                .collect(Collectors.toList());

        // Récupérer les nouveaux employés embauchés durant l'année
        Integer finalYear = year;
        List<Employee> newHires = employeeRepository.findAll().stream()
                .filter(e -> e.getHireDate() != null)
                .filter(e -> e.getHireDate().getYear() == finalYear)
                .collect(Collectors.toList());

        // Récupérer les employés qui ont quitté l'entreprise durant l'année
        List<Employee> departures = employeeRepository.findAll().stream()
                .filter(e -> e.getEndDate() != null)
                .filter(e -> e.getEndDate().getYear() == finalYear)
                .collect(Collectors.toList());

        // Calculer le taux de turnover annuel
        double turnoverRate = calculateTurnoverRate(
                startEmployees.size(), endEmployees.size(), departures.size());

        // Calculer le turnover mensuel
        List<MonthlyTurnoverDTO> monthlyTurnover = calculateMonthlyTurnover(year, startEmployees);

        // Calculer le turnover par département
        List<DepartmentTurnoverDTO> departmentTurnover = calculateDepartmentTurnover(year, startEmployees);

        // Construire et retourner le DTO de rapport
        return TurnoverReportDTO.builder()
                .year(year)
                .startEmployeeCount(startEmployees.size())
                .endEmployeeCount(endEmployees.size())
                .newHires(newHires.size())
                .departures(departures.size())
                .turnoverRate(roundToTwoDecimals(turnoverRate))
                .monthlyTurnover(monthlyTurnover)
                .departmentTurnover(departmentTurnover)
                .build();
    }

    @Override
    public AbsenteeismReportDTO generateAbsenteeismReport(Integer year) {
        if (year == null) {
            year = Year.now().getValue();
        }

        // Définir les dates de début et de fin d'année
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        return generateAbsenteeismReportForPeriod(startDate, endDate);
    }

    @Override
    public AbsenteeismReportDTO generateAbsenteeismReportForPeriod(LocalDate startDate, LocalDate endDate) {
        // Récupérer tous les employés actifs durant la période
        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getHireDate() != null && e.getHireDate().isBefore(endDate))
                .filter(e -> e.getEndDate() == null || e.getEndDate().isAfter(startDate))
                .collect(Collectors.toList());

        // Récupérer toutes les absences approuvées durant la période
        List<Leave> approvedLeaves = leaveRepository.findLeavesInPeriod(startDate, endDate).stream()
                .filter(leave -> leave.getStatus() == LeaveStatus.APPROVED
                        || leave.getStatus() == LeaveStatus.COMPLETED)
                .collect(Collectors.toList());

        // Calculer le nombre total de jours ouvrables dans la période
        int totalWorkingDays = calculateWorkingDays(startDate, endDate);

        // Calculer le nombre total de jours d'absence
        int totalAbsenceDays = approvedLeaves.stream()
                .mapToInt(leave -> leave.getDurationDays() != null ? leave.getDurationDays() : 0)
                .sum();

        // Calculer le taux d'absentéisme
        double absenteeismRate = calculateAbsenteeismRate(
                activeEmployees.size(), totalWorkingDays, totalAbsenceDays);

        // Calculer l'absentéisme mensuel
        List<MonthlyAbsenteeismDTO> monthlyAbsenteeism = calculateMonthlyAbsenteeism(
                startDate, endDate, activeEmployees, approvedLeaves);

        // Calculer l'absentéisme par département
        List<DepartmentAbsenteeismDTO> departmentAbsenteeism = calculateDepartmentAbsenteeism(
                startDate, endDate, activeEmployees, approvedLeaves);

        // Calculer l'absentéisme par type de congé
        List<LeaveTypeAbsenteeismDTO> leaveTypeAbsenteeism = calculateLeaveTypeAbsenteeism(
                approvedLeaves, totalAbsenceDays);

        // Construire et retourner le DTO de rapport
        return AbsenteeismReportDTO.builder()
                .year(startDate.getYear())
                .totalEmployees(activeEmployees.size())
                .totalWorkingDays(totalWorkingDays)
                .totalAbsenceDays(totalAbsenceDays)
                .absenteeismRate(roundToTwoDecimals(absenteeismRate))
                .monthlyAbsenteeism(monthlyAbsenteeism)
                .departmentAbsenteeism(departmentAbsenteeism)
                .leaveTypeAbsenteeism(leaveTypeAbsenteeism)
                .build();
    }

    @Override
    public SalaryCostReportDTO generateSalaryCostReport(Integer year) {
        if (year == null) {
            year = Year.now().getValue();
        }

        // Définir les dates de début et de fin d'année
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        // Récupérer tous les contrats actifs durant l'année
        List<Contract> activeContracts = contractRepository.findAll().stream()
                .filter(c -> c.getStartDate() != null && c.getStartDate().isBefore(endDate))
                .filter(c -> c.getEndDate() == null || c.getEndDate().isAfter(startDate))
                .collect(Collectors.toList());

        // Calculer les coûts salariaux totaux
        BigDecimal totalSalaryCost = activeContracts.stream()
                .map(Contract::getSalary)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculer le salaire moyen
        BigDecimal averageSalary = activeContracts.isEmpty() ? BigDecimal.ZERO :
                totalSalaryCost.divide(BigDecimal.valueOf(activeContracts.size()), 2, RoundingMode.HALF_UP);

        // Calculer le salaire médian
        BigDecimal medianSalary = calculateMedianSalary(activeContracts);

        // Trouver les salaires min et max
        BigDecimal minSalary = activeContracts.stream()
                .map(Contract::getSalary)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal maxSalary = activeContracts.stream()
                .map(Contract::getSalary)
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // Calculer les coûts salariaux mensuels
        List<MonthlySalaryCostDTO> monthlyCosts = calculateMonthlySalaryCosts(year, activeContracts);

        // Calculer les coûts salariaux par département
        List<DepartmentSalaryCostDTO> departmentCosts = calculateDepartmentSalaryCosts(activeContracts, totalSalaryCost);

        // Calculer les coûts par type de contrat
        Map<String, BigDecimal> costByContractType = calculateCostByContractType(activeContracts);

        // Construire et retourner le DTO de rapport
        return SalaryCostReportDTO.builder()
                .year(year)
                .totalSalaryCost(totalSalaryCost)
                .averageSalary(averageSalary)
                .medianSalary(medianSalary)
                .minSalary(minSalary)
                .maxSalary(maxSalary)
                .monthlyCosts(monthlyCosts)
                .departmentCosts(departmentCosts)
                .costByContractType(costByContractType)
                .build();
    }

    @Override
    public EmployeeDistributionReportDTO generateEmployeeDistributionReport(LocalDate referenceDate) {
        if (referenceDate == null) {
            referenceDate = LocalDate.now();
        }

        // Récupérer tous les employés actifs à la date de référence
        LocalDate finalReferenceDate1 = referenceDate;
        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getHireDate() != null && e.getHireDate().isBefore(finalReferenceDate1.plusDays(1)))
                .filter(e -> e.getEndDate() == null || e.getEndDate().isAfter(finalReferenceDate1))
                .collect(Collectors.toList());

        // Calculer la distribution par statut
        Map<Status, Integer> statusDistribution = calculateStatusDistribution(activeEmployees);

        // Récupérer les contrats actifs à la date de référence
        LocalDate finalReferenceDate = referenceDate;
        List<Contract> activeContracts = contractRepository.findAll().stream()
                .filter(c -> c.getStartDate() != null && c.getStartDate().isBefore(finalReferenceDate.plusDays(1)))
                .filter(c -> c.getEndDate() == null || c.getEndDate().isAfter(finalReferenceDate))
                .collect(Collectors.toList());

        // Calculer la distribution par type de contrat
        Map<sn.bmbacke.rh.entity.enums.Type, Integer> contractTypeDistribution =
                calculateContractTypeDistribution(activeContracts);

        // Calculer la distribution par département
        Map<String, Integer> departmentDistribution = calculateDepartmentDistribution(activeEmployees);

        // Calculer la distribution par poste
        Map<String, Integer> positionDistribution = calculatePositionDistribution(activeEmployees);

        // Calculer la distribution par genre
        GenderDistributionDTO genderDistribution = calculateGenderDistribution(activeEmployees);

        // Calculer la distribution par âge
        AgeDistributionDTO ageDistribution = calculateAgeDistribution(activeEmployees, referenceDate);

        // Calculer la distribution par ancienneté
        SeniorityDistributionDTO seniorityDistribution = calculateSeniorityDistribution(activeEmployees, referenceDate);

        // Construire et retourner le DTO de rapport
        return EmployeeDistributionReportDTO.builder()
                .totalEmployees(activeEmployees.size())
                .statusDistribution(statusDistribution)
                .contractTypeDistribution(contractTypeDistribution)
                .departmentDistribution(departmentDistribution)
                .positionDistribution(positionDistribution)
                .genderDistribution(genderDistribution)
                .ageDistribution(ageDistribution)
                .seniorityDistribution(seniorityDistribution)
                .build();
    }

    @Override
    public EmployeeDistributionReportDTO generateCurrentEmployeeDistributionReport() {
        return generateEmployeeDistributionReport(LocalDate.now());
    }

    @Override
    public byte[] exportReport(Object reportData, String reportType, String format) {
        if (format == null) {
            format = "PDF"; // Format par défaut
        }

        switch (format.toUpperCase()) {
            case "PDF":
                Map<String, Object> data = new HashMap<>();
                data.put("report", reportData);
                data.put("reportType", reportType);
                data.put("generatedDate", LocalDate.now());
                return documentGenerationService.generatePDF(reportType + "Template", data);

            case "EXCEL":
                return documentGenerationService.generateExcel(reportType, reportData);

            case "CSV":
                return documentGenerationService.generateCSV(reportData);

            default:
                throw new BusinessException("Format non pris en charge : " + format);
        }
    }

    /**
     * Calcule le taux de turnover
     */
    private double calculateTurnoverRate(int startCount, int endCount, int departures) {
        if (startCount == 0 && endCount == 0) {
            return 0.0;
        }

        double avgEmployeeCount = (startCount + endCount) / 2.0;
        return (departures / avgEmployeeCount) * 100.0;
    }

    /**
     * Calcule le turnover mensuel
     */
    private List<MonthlyTurnoverDTO> calculateMonthlyTurnover(int year, List<Employee> baseEmployees) {
        List<MonthlyTurnoverDTO> result = new ArrayList<>();

        for (Month month : Month.values()) {
            LocalDate startOfMonth = LocalDate.of(year, month, 1);
            LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

            // Employés au début du mois
            int startMonthCount = (int) baseEmployees.stream()
                    .filter(e -> e.getHireDate().isBefore(startOfMonth))
                    .filter(e -> e.getEndDate() == null || e.getEndDate().isAfter(startOfMonth))
                    .count();

            // Employés à la fin du mois
            int endMonthCount = (int) baseEmployees.stream()
                    .filter(e -> e.getHireDate().isBefore(endOfMonth.plusDays(1)))
                    .filter(e -> e.getEndDate() == null || e.getEndDate().isAfter(endOfMonth))
                    .count();

            // Nouveaux employés dans le mois
            int newHires = (int) baseEmployees.stream()
                    .filter(e -> e.getHireDate().getYear() == year && e.getHireDate().getMonth() == month)
                    .count();

            // Départs dans le mois
            int departures = (int) baseEmployees.stream()
                    .filter(e -> e.getEndDate() != null)
                    .filter(e -> e.getEndDate().getYear() == year && e.getEndDate().getMonth() == month)
                    .count();

            // Calculer le taux de turnover mensuel
            double monthlyRate = calculateTurnoverRate(startMonthCount, endMonthCount, departures);

            result.add(MonthlyTurnoverDTO.builder()
                    .year(year)
                    .month(month.getValue())
                    .monthName(month.getDisplayName(TextStyle.FULL, Locale.getDefault()))
                    .newHires(newHires)
                    .departures(departures)
                    .turnoverRate(roundToTwoDecimals(monthlyRate))
                    .build());
        }

        return result;
    }

    /**
     * Calcule le turnover par département
     */
    private List<DepartmentTurnoverDTO> calculateDepartmentTurnover(int year, List<Employee> baseEmployees) {
        List<DepartmentTurnoverDTO> result = new ArrayList<>();
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);

        // Récupérer tous les départements
        List<Departement> departments = departmentRepository.findAll();

        for (Departement department : departments) {
            // Employés au début de l'année
            int startYearCount = (int) baseEmployees.stream()
                    .filter(e -> e.getDepartement() != null && e.getDepartement().getId().equals(department.getId()))
                    .filter(e -> e.getHireDate().isBefore(startOfYear))
                    .filter(e -> e.getEndDate() == null || e.getEndDate().isAfter(startOfYear))
                    .count();

            // Employés à la fin de l'année
            int endYearCount = (int) baseEmployees.stream()
                    .filter(e -> e.getDepartement() != null && e.getDepartement().getId().equals(department.getId()))
                    .filter(e -> e.getHireDate().isBefore(endOfYear.plusDays(1)))
                    .filter(e -> e.getEndDate() == null || e.getEndDate().isAfter(endOfYear))
                    .count();

            // Nouveaux employés dans l'année
            int newHires = (int) baseEmployees.stream()
                    .filter(e -> e.getDepartement() != null && e.getDepartement().getId().equals(department.getId()))
                    .filter(e -> e.getHireDate().getYear() == year)
                    .count();

            // Départs dans l'année
            int departures = (int) baseEmployees.stream()
                    .filter(e -> e.getDepartement() != null && e.getDepartement().getId().equals(department.getId()))
                    .filter(e -> e.getEndDate() != null && e.getEndDate().getYear() == year)
                    .count();

            // Calculer le taux de turnover du département
            double departmentRate = calculateTurnoverRate(startYearCount, endYearCount, departures);

            result.add(DepartmentTurnoverDTO.builder()
                    .departmentId(department.getId())
                    .departmentName(department.getName())
                    .startEmployeeCount(startYearCount)
                    .endEmployeeCount(endYearCount)
                    .newHires(newHires)
                    .departures(departures)
                    .turnoverRate(roundToTwoDecimals(departmentRate))
                    .build());
        }

        return result;
    }

    /**
     * Calcule le nombre de jours ouvrables dans une période
     */
    private int calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        int workingDays = 0;
        LocalDate date = startDate;

        while (!date.isAfter(endDate)) {
            if (date.getDayOfWeek().getValue() < 6) { // Du lundi au vendredi
                workingDays++;
            }
            date = date.plusDays(1);
        }

        return workingDays;
    }

    /**
     * Calcule le taux d'absentéisme
     */
    private double calculateAbsenteeismRate(int totalEmployees, int totalWorkingDays, int totalAbsenceDays) {
        if (totalEmployees == 0 || totalWorkingDays == 0) {
            return 0.0;
        }

        int possibleWorkingDays = totalEmployees * totalWorkingDays;
        return ((double) totalAbsenceDays / possibleWorkingDays) * 100.0;
    }

    /**
     * Utilitaire pour arrondir à deux décimales
     */
    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Calcule le salaire médian
     */
    private BigDecimal calculateMedianSalary(List<Contract> contracts) {
        if (contracts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<BigDecimal> salaries = contracts.stream()
                .map(Contract::getSalary)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        int size = salaries.size();
        if (size == 0) {
            return BigDecimal.ZERO;
        }

        if (size % 2 == 0) {
            // Pour un nombre pair d'éléments, la médiane est la moyenne des deux éléments du milieu
            BigDecimal middle1 = salaries.get(size / 2 - 1);
            BigDecimal middle2 = salaries.get(size / 2);
            return middle1.add(middle2).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        } else {
            // Pour un nombre impair d'éléments, la médiane est l'élément du milieu
            return salaries.get(size / 2);
        }
    }


    /**
     * Méthodes pour calculer l'absentéisme mensuel, par département, etc.
     */
    /**
     * Calcule l'absentéisme mensuel
     */
    private List<MonthlyAbsenteeismDTO> calculateMonthlyAbsenteeism(
            LocalDate startDate, LocalDate endDate, List<Employee> activeEmployees, List<Leave> approvedLeaves) {

        List<MonthlyAbsenteeismDTO> result = new ArrayList<>();

        // Parcourir tous les mois dans la période
        YearMonth current = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(endDate);

        while (!current.isAfter(end)) {
            // Dates de début et fin du mois
            LocalDate monthStart = current.atDay(1);
            LocalDate monthEnd = current.atEndOfMonth();

            // Ajuster selon les dates de début/fin fournies
            if (monthStart.isBefore(startDate)) {
                monthStart = startDate;
            }
            if (monthEnd.isAfter(endDate)) {
                monthEnd = endDate;
            }

            // Calculer les jours ouvrables pour ce mois
            int workingDays = calculateWorkingDays(monthStart, monthEnd);

            // Nombre d'employés actifs durant ce mois
            int employeeCount = activeEmployees.size();

            // Calculer les jours d'absence pour ce mois
            int absenceDays = 0;
            for (Leave leave : approvedLeaves) {
                // Vérifier si le congé chevauche le mois courant
                if (!leave.getEndDate().isBefore(monthStart) && !leave.getStartDate().isAfter(monthEnd)) {
                    // Calculer le chevauchement entre la période de congé et le mois courant
                    LocalDate overlapStart = leave.getStartDate().isBefore(monthStart) ? monthStart : leave.getStartDate();
                    LocalDate overlapEnd = leave.getEndDate().isAfter(monthEnd) ? monthEnd : leave.getEndDate();

                    // Compter uniquement les jours ouvrables
                    absenceDays += calculateWorkingDays(overlapStart, overlapEnd);
                }
            }

            // Calculer le taux d'absentéisme
            double absenteeismRate = calculateAbsenteeismRate(employeeCount, workingDays, absenceDays);

            // Créer le DTO pour ce mois
            MonthlyAbsenteeismDTO monthlyDTO = MonthlyAbsenteeismDTO.builder()
                    .year(current.getYear())
                    .month(current.getMonthValue())
                    .monthName(current.getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE))
                    .totalEmployees(employeeCount)
                    .workingDays(workingDays)
                    .absenceDays(absenceDays)
                    .absenteeismRate(roundToTwoDecimals(absenteeismRate))
                    .build();

            result.add(monthlyDTO);

            // Passer au mois suivant
            current = current.plusMonths(1);
        }

        return result;
    }

    private List<DepartmentAbsenteeismDTO> calculateDepartmentAbsenteeism(
            LocalDate startDate, LocalDate endDate, List<Employee> activeEmployees, List<Leave> approvedLeaves) {

        List<DepartmentAbsenteeismDTO> result = new ArrayList<>();

        // Calculate total working days in the period
        int totalWorkingDays = calculateWorkingDays(startDate, endDate);

        // Get all departments
        List<Departement> departments = departmentRepository.findAll();

        for (Departement department : departments) {
            // Get employees for this department
            List<Employee> departmentEmployees = activeEmployees.stream()
                    .filter(e -> e.getDepartement() != null &&
                            e.getDepartement().getId().equals(department.getId()))
                    .collect(Collectors.toList());

            int employeeCount = departmentEmployees.size();

            // Skip departments with no employees
            if (employeeCount == 0) {
                continue;
            }

            // Get employee IDs for this department
            Set<Long> departmentEmployeeIds = departmentEmployees.stream()
                    .map(Employee::getId)
                    .collect(Collectors.toSet());

            // Calculate absence days for this department
            int absenceDays = 0;

            for (Leave leave : approvedLeaves) {
                // Check if leave belongs to an employee in this department
                if (leave.getEmployee() != null &&
                        departmentEmployeeIds.contains(leave.getEmployee().getId())) {

                    // Calculate the overlap between leave period and the reporting period
                    LocalDate overlapStart = leave.getStartDate().isBefore(startDate) ?
                            startDate : leave.getStartDate();
                    LocalDate overlapEnd = leave.getEndDate().isAfter(endDate) ?
                            endDate : leave.getEndDate();

                    // Count only working days
                    absenceDays += calculateWorkingDays(overlapStart, overlapEnd);
                }
            }

            // Calculate absenteeism rate
            double absenteeismRate = calculateAbsenteeismRate(employeeCount, totalWorkingDays, absenceDays);

            // Create DTO for this department
            DepartmentAbsenteeismDTO departmentDTO = DepartmentAbsenteeismDTO.builder()
                    .departmentId(department.getId())
                    .departmentName(department.getName())
                    .totalEmployees(employeeCount)
                    .absenceDays(absenceDays)
                    .absenteeismRate(roundToTwoDecimals(absenteeismRate))
                    .build();

            result.add(departmentDTO);
        }

        // Sort by department name for consistency
        result.sort(Comparator.comparing(DepartmentAbsenteeismDTO::getDepartmentName));

        return result;
    }

    /**
     * Calcule la répartition des absences par type de congé
     */
    private List<LeaveTypeAbsenteeismDTO> calculateLeaveTypeAbsenteeism(
            List<Leave> approvedLeaves, int totalAbsenceDays) {

        List<LeaveTypeAbsenteeismDTO> result = new ArrayList<>();

        // Regrouper les congés par type
        Map<LeaveType, List<Leave>> leavesByType = approvedLeaves.stream()
                .collect(Collectors.groupingBy(Leave::getLeaveType));

        // Pour chaque type de congé, calculer le nombre total de jours et le pourcentage
        for (Map.Entry<LeaveType, List<Leave>> entry : leavesByType.entrySet()) {
            LeaveType leaveType = entry.getKey();
            List<Leave> leaves = entry.getValue();

            // Calculer le nombre total de jours d'absence pour ce type
            int absenceDays = leaves.stream()
                    .mapToInt(leave -> leave.getDurationDays() != null ? leave.getDurationDays() : 0)
                    .sum();

            // Calculer le pourcentage par rapport au total
            double percentage = totalAbsenceDays > 0 ?
                    roundToTwoDecimals(((double) absenceDays / totalAbsenceDays) * 100) : 0.0;

            // Créer le DTO
            LeaveTypeAbsenteeismDTO dto = LeaveTypeAbsenteeismDTO.builder()
                    .leaveType(leaveType)
                    .leaveTypeName(leaveType.getDisplayName())
                    .absenceDays(absenceDays)
                    .percentageOfTotal(percentage)
                    .build();

            result.add(dto);
        }

        // Trier par nombre de jours d'absence décroissant
        result.sort(Comparator.comparing(LeaveTypeAbsenteeismDTO::getAbsenceDays).reversed());

        return result;
    }

    /**
     * Méthodes pour calculer les coûts salariaux mensuels, par département, etc.
     */
    /**
     * Calcule les coûts salariaux mensuels
     */
    private List<MonthlySalaryCostDTO> calculateMonthlySalaryCosts(int year, List<Contract> activeContracts) {
        List<MonthlySalaryCostDTO> result = new ArrayList<>();

        // Pour chaque mois de l'année
        for (int month = 1; month <= 12; month++) {
            // Déterminer le début et la fin du mois
            LocalDate startOfMonth = LocalDate.of(year, month, 1);
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

            // Filtrer les contrats actifs pendant ce mois
            List<Contract> contractsActiveInMonth = activeContracts.stream()
                    .filter(contract -> {
                        LocalDate startDate = contract.getStartDate();
                        LocalDate endDate = contract.getEndDate() != null ? contract.getEndDate() : LocalDate.MAX;

                        // Le contrat est actif si sa période chevauche le mois en cours
                        return !startDate.isAfter(endOfMonth) && !endDate.isBefore(startOfMonth);
                    })
                    .collect(Collectors.toList());

            // Nombre d'employés ce mois
            int employeeCount = contractsActiveInMonth.size();

            // Calculer le coût total et le salaire moyen
            BigDecimal totalCost = BigDecimal.ZERO;
            BigDecimal averageSalary = BigDecimal.ZERO;

            if (employeeCount > 0) {
                totalCost = contractsActiveInMonth.stream()
                        .map(Contract::getSalary)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                averageSalary = totalCost.divide(BigDecimal.valueOf(employeeCount), 2, RoundingMode.HALF_UP);
            }

            // Obtenir le nom du mois
            String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.FRANCE);

            // Créer le DTO
            MonthlySalaryCostDTO monthlyCost = MonthlySalaryCostDTO.builder()
                    .year(year)
                    .month(month)
                    .monthName(monthName)
                    .totalCost(totalCost)
                    .employeeCount(employeeCount)
                    .averageSalary(averageSalary)
                    .build();

            result.add(monthlyCost);
        }

        return result;
    }

    private List<DepartmentSalaryCostDTO> calculateDepartmentSalaryCosts(
            List<Contract> activeContracts, BigDecimal totalSalaryCost) {

        List<DepartmentSalaryCostDTO> result = new ArrayList<>();

        // Group contracts by department
        Map<Departement, List<Contract>> contractsByDepartment = activeContracts.stream()
                .filter(contract -> contract.getEmployee() != null &&
                        contract.getEmployee().getDepartement() != null)
                .collect(Collectors.groupingBy(contract -> contract.getEmployee().getDepartement()));

        // Calculate costs for each department
        for (Map.Entry<Departement, List<Contract>> entry : contractsByDepartment.entrySet()) {
            Departement department = entry.getKey();
            List<Contract> departmentContracts = entry.getValue();

            // Count employees in this department
            int employeeCount = departmentContracts.size();

            // Calculate total cost for this department
            BigDecimal departmentTotalCost = departmentContracts.stream()
                    .map(Contract::getSalary)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate average salary
            BigDecimal averageSalary = employeeCount > 0
                    ? departmentTotalCost.divide(BigDecimal.valueOf(employeeCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // Calculate percentage of total cost
            Double percentageOfTotal = totalSalaryCost.compareTo(BigDecimal.ZERO) > 0
                    ? departmentTotalCost.multiply(BigDecimal.valueOf(100))
                    .divide(totalSalaryCost, 2, RoundingMode.HALF_UP)
                    .doubleValue()
                    : 0.0;

            // Create DTO
            DepartmentSalaryCostDTO dto = DepartmentSalaryCostDTO.builder()
                    .departmentId(department.getId())
                    .departmentName(department.getName())
                    .totalCost(departmentTotalCost)
                    .employeeCount(employeeCount)
                    .averageSalary(averageSalary)
                    .percentageOfTotal(percentageOfTotal)
                    .build();

            result.add(dto);
        }

        // Sort by department name
        result.sort(Comparator.comparing(DepartmentSalaryCostDTO::getDepartmentName));

        return result;
    }

    /**
     * Calcule le coût total des salaires par type de contrat
     * @param activeContracts Liste des contrats actifs
     * @return Map avec le type de contrat comme clé et le coût total comme valeur
     */
    private Map<String, BigDecimal> calculateCostByContractType(List<Contract> activeContracts) {
        if (activeContracts == null || activeContracts.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, BigDecimal> costByType = new HashMap<>();

        for (Contract contract : activeContracts) {
            if (contract.getType() != null && contract.getSalary() != null) {
                String contractType = contract.getType().name();
                BigDecimal currentAmount = costByType.getOrDefault(contractType, BigDecimal.ZERO);
                costByType.put(contractType, currentAmount.add(contract.getSalary()));
            }
        }

        return costByType;
    }

    /**
     * Méthodes pour calculer les distributions des employés
     */
    private Map<Status, Integer> calculateStatusDistribution(List<Employee> activeEmployees) {
        if (activeEmployees == null || activeEmployees.isEmpty()) {
            return new HashMap<>();
        }

        Map<Status, Integer> statusDistribution = new HashMap<>();

        // Initialize with all status values to ensure complete reporting
        for (Status status : Status.values()) {
            statusDistribution.put(status, 0);
        }

        // Count employees for each status
        for (Employee employee : activeEmployees) {
            Status status = employee.getStatus();
            if (status != null) {
                statusDistribution.put(status, statusDistribution.get(status) + 1);
            }
        }

        return statusDistribution;
    }

    private Map<sn.bmbacke.rh.entity.enums.Type, Integer> calculateContractTypeDistribution(
            List<Contract> activeContracts) {
        if (activeContracts == null || activeContracts.isEmpty()) {
            return new HashMap<>();
        }

        Map<sn.bmbacke.rh.entity.enums.Type, Integer> typeDistribution = new HashMap<>();

        // Initialize with all contract types to ensure complete reporting
        for (sn.bmbacke.rh.entity.enums.Type type : sn.bmbacke.rh.entity.enums.Type.values()) {
            typeDistribution.put(type, 0);
        }

        // Count contracts for each type
        for (Contract contract : activeContracts) {
            sn.bmbacke.rh.entity.enums.Type type = contract.getType();
            if (type != null) {
                typeDistribution.put(type, typeDistribution.get(type) + 1);
            }
        }

        return typeDistribution;
    }

    private Map<String, Integer> calculateDepartmentDistribution(List<Employee> activeEmployees) {
        if (activeEmployees == null || activeEmployees.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Integer> departmentDistribution = new HashMap<>();

        // Count employees for each department
        for (Employee employee : activeEmployees) {
            Departement departement = employee.getDepartement();
            String departmentName = departement != null ? departement.getName() : "Non assigné";

            // Increment count for this department
            departmentDistribution.put(departmentName,
                    departmentDistribution.getOrDefault(departmentName, 0) + 1);
        }

        return departmentDistribution;
    }

    private Map<String, Integer> calculatePositionDistribution(List<Employee> activeEmployees) {
        if (activeEmployees == null || activeEmployees.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Integer> positionDistribution = new HashMap<>();

        // Count employees for each position
        for (Employee employee : activeEmployees) {
            Position position = employee.getPosition();
            String positionName = position != null ? position.getTitle() : "Non assigné";

            // Increment count for this position
            positionDistribution.put(positionName,
                    positionDistribution.getOrDefault(positionName, 0) + 1);
        }

        return positionDistribution;
    }

    private GenderDistributionDTO calculateGenderDistribution(List<Employee> activeEmployees) {
        int maleCount = (int) activeEmployees.stream()
                .filter(e -> e.getGender() == Gender.MALE)
                .count();

        int femaleCount = (int) activeEmployees.stream()
                .filter(e -> e.getGender() == Gender.FEMALE)
                .count();

        double malePercentage = activeEmployees.isEmpty() ? 0 :
                roundToTwoDecimals((double) maleCount / activeEmployees.size() * 100);

        double femalePercentage = activeEmployees.isEmpty() ? 0 :
                roundToTwoDecimals((double) femaleCount / activeEmployees.size() * 100);

        // Calculate gender distribution by department
        Map<String, GenderDistributionDTO> byDepartment = activeEmployees.stream()
                .filter(e -> e.getDepartement() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getDepartement().getName(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::calculateGenderDistributionForGroup
                        )
                ));

        // Calculate gender distribution by position
        Map<String, GenderDistributionDTO> byPosition = activeEmployees.stream()
                .filter(e -> e.getPosition() != null)
                .collect(Collectors.groupingBy(
                        e -> e.getPosition().getTitle(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::calculateGenderDistributionForGroup
                        )
                ));

        return GenderDistributionDTO.builder()
                .maleCount(maleCount)
                .femaleCount(femaleCount)
                .malePercentage(malePercentage)
                .femalePercentage(femalePercentage)
                .byDepartment(byDepartment)
                .byPosition(byPosition)
                .build();
    }

    /**
     * Helper method to calculate gender distribution for a group of employees
     */
    private GenderDistributionDTO calculateGenderDistributionForGroup(List<Employee> employeeGroup) {
        int maleCount = (int) employeeGroup.stream()
                .filter(e -> e.getGender() == Gender.MALE)
                .count();

        int femaleCount = (int) employeeGroup.stream()
                .filter(e -> e.getGender() == Gender.FEMALE)
                .count();

        double malePercentage = employeeGroup.isEmpty() ? 0 :
                roundToTwoDecimals((double) maleCount / employeeGroup.size() * 100);

        double femalePercentage = employeeGroup.isEmpty() ? 0 :
                roundToTwoDecimals((double) femaleCount / employeeGroup.size() * 100);

        return GenderDistributionDTO.builder()
                .maleCount(maleCount)
                .femaleCount(femaleCount)
                .malePercentage(malePercentage)
                .femalePercentage(femalePercentage)
                .byDepartment(new HashMap<>())
                .byPosition(new HashMap<>())
                .build();
    }

    private AgeDistributionDTO calculateAgeDistribution(List<Employee> activeEmployees, LocalDate referenceDate) {
        // If there are no active employees, return empty distribution
        if (activeEmployees == null || activeEmployees.isEmpty()) {
            return AgeDistributionDTO.builder()
                    .averageAge(0.0)
                    .under25Count(0)
                    .age25to34Count(0)
                    .age35to44Count(0)
                    .age45to54Count(0)
                    .age55plusCount(0)
                    .averageAgeByDepartment(new HashMap<>())
                    .build();
        }

        // Count employees in each age group
        int under25Count = 0;
        int age25to34Count = 0;
        int age35to44Count = 0;
        int age45to54Count = 0;
        int age55plusCount = 0;

        // Track total age for average calculation
        double totalAge = 0;
        int employeesWithAge = 0;

        // Track age by department
        Map<String, List<Double>> agesByDepartment = new HashMap<>();

        // Process each employee
        for (Employee employee : activeEmployees) {
            // Skip employees without birth date
            if (employee.getBirthDate() == null) {
                continue;
            }

            // Calculate age as of reference date
            int age = calculateAge(employee.getBirthDate(), referenceDate);
            totalAge += age;
            employeesWithAge++;

            // Categorize by age group
            if (age < 25) {
                under25Count++;
            } else if (age < 35) {
                age25to34Count++;
            } else if (age < 45) {
                age35to44Count++;
            } else if (age < 55) {
                age45to54Count++;
            } else {
                age55plusCount++;
            }

            // Track ages by department
            if (employee.getDepartement() != null) {
                String departmentName = employee.getDepartement().getName();
                agesByDepartment.putIfAbsent(departmentName, new ArrayList<>());
                agesByDepartment.get(departmentName).add((double) age);
            }
        }

        // Calculate average age
        double averageAge = employeesWithAge > 0 ?
                roundToTwoDecimals(totalAge / employeesWithAge) : 0.0;

        // Calculate average age by department
        Map<String, Double> averageAgeByDepartment = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : agesByDepartment.entrySet()) {
            String departmentName = entry.getKey();
            List<Double> ages = entry.getValue();
            double departmentAverageAge = ages.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            averageAgeByDepartment.put(departmentName, roundToTwoDecimals(departmentAverageAge));
        }

        return AgeDistributionDTO.builder()
                .averageAge(averageAge)
                .under25Count(under25Count)
                .age25to34Count(age25to34Count)
                .age35to44Count(age35to44Count)
                .age45to54Count(age45to54Count)
                .age55plusCount(age55plusCount)
                .averageAgeByDepartment(averageAgeByDepartment)
                .build();
    }

    /**
     * Calculate age of a person based on birth date and reference date
     */
    private int calculateAge(LocalDate birthDate, LocalDate referenceDate) {
        // Handle edge case where birth date is after reference date
        if (birthDate.isAfter(referenceDate)) {
            return 0;
        }

        int age = referenceDate.getYear() - birthDate.getYear();

        // Adjust age if birthday hasn't occurred yet in the reference year
        if (birthDate.getMonthValue() > referenceDate.getMonthValue() ||
                (birthDate.getMonthValue() == referenceDate.getMonthValue() &&
                        birthDate.getDayOfMonth() > referenceDate.getDayOfMonth())) {
            age--;
        }

        return age;
    }

    private SeniorityDistributionDTO calculateSeniorityDistribution(
            List<Employee> activeEmployees, LocalDate referenceDate) {
        // If there are no active employees, return empty distribution
        if (activeEmployees == null || activeEmployees.isEmpty()) {
            return SeniorityDistributionDTO.builder()
                    .averageSeniority(0.0)
                    .lessThan1YearCount(0)
                    .oneToThreeYearsCount(0)
                    .threeToFiveYearsCount(0)
                    .fiveToTenYearsCount(0)
                    .moreThanTenYearsCount(0)
                    .averageSeniorityByDepartment(new HashMap<>())
                    .build();
        }

        // Count employees in each seniority group
        int lessThan1YearCount = 0;
        int oneToThreeYearsCount = 0;
        int threeToFiveYearsCount = 0;
        int fiveToTenYearsCount = 0;
        int moreThanTenYearsCount = 0;

        // Track total seniority for average calculation
        double totalSeniority = 0;
        int employeesWithHireDate = 0;

        // Track seniority by department
        Map<String, List<Double>> seniorityByDepartment = new HashMap<>();

        // Process each employee
        for (Employee employee : activeEmployees) {
            if (employee.getHireDate() != null) {
                // Calculate seniority in years
                double seniority = calculateSeniorityInYears(employee.getHireDate(), referenceDate);

                // Update counters based on seniority
                if (seniority < 1.0) {
                    lessThan1YearCount++;
                } else if (seniority < 3.0) {
                    oneToThreeYearsCount++;
                } else if (seniority < 5.0) {
                    threeToFiveYearsCount++;
                } else if (seniority < 10.0) {
                    fiveToTenYearsCount++;
                } else {
                    moreThanTenYearsCount++;
                }

                // Add to total for average calculation
                totalSeniority += seniority;
                employeesWithHireDate++;

                // Track by department
                if (employee.getDepartement() != null) {
                    String departmentName = employee.getDepartement().getName();
                    seniorityByDepartment.computeIfAbsent(departmentName, k -> new ArrayList<>()).add(seniority);
                }
            }
        }

        // Calculate average seniority
        double averageSeniority = employeesWithHireDate > 0 ?
                roundToTwoDecimals(totalSeniority / employeesWithHireDate) : 0.0;

        // Calculate average seniority by department
        Map<String, Double> averageSeniorityByDepartment = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : seniorityByDepartment.entrySet()) {
            double departmentAverage = entry.getValue().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            averageSeniorityByDepartment.put(entry.getKey(), roundToTwoDecimals(departmentAverage));
        }

        return SeniorityDistributionDTO.builder()
                .averageSeniority(averageSeniority)
                .lessThan1YearCount(lessThan1YearCount)
                .oneToThreeYearsCount(oneToThreeYearsCount)
                .threeToFiveYearsCount(threeToFiveYearsCount)
                .fiveToTenYearsCount(fiveToTenYearsCount)
                .moreThanTenYearsCount(moreThanTenYearsCount)
                .averageSeniorityByDepartment(averageSeniorityByDepartment)
                .build();
    }

    /**
     * Calculate seniority in years based on hire date and reference date
     */
    private double calculateSeniorityInYears(LocalDate hireDate, LocalDate referenceDate) {
        // Handle edge case where hire date is after reference date
        if (hireDate.isAfter(referenceDate)) {
            return 0.0;
        }

        // Calculate years between the two dates
        int years = referenceDate.getYear() - hireDate.getYear();

        // Adjust if anniversary hasn't occurred yet in the reference year
        if (hireDate.getMonthValue() > referenceDate.getMonthValue() ||
                (hireDate.getMonthValue() == referenceDate.getMonthValue() &&
                        hireDate.getDayOfMonth() > referenceDate.getDayOfMonth())) {
            years--;
        }

        // Add fractional year component for more accuracy
        LocalDate anniversaryThisYear = hireDate.withYear(referenceDate.getYear());
        if (anniversaryThisYear.isAfter(referenceDate)) {
            anniversaryThisYear = anniversaryThisYear.minusYears(1);
        }

        LocalDate nextAnniversary = anniversaryThisYear.plusYears(1);
        double daysTotal = nextAnniversary.toEpochDay() - anniversaryThisYear.toEpochDay();
        double daysPassed = referenceDate.toEpochDay() - anniversaryThisYear.toEpochDay();
        double fractionOfYear = daysPassed / daysTotal;

        return years + fractionOfYear;
    }
}