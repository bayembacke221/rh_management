package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map; /**
 * DTO pour le rapport des coûts salariaux
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryCostReportDTO {
    private Integer year;
    private BigDecimal totalSalaryCost;
    private BigDecimal averageSalary;
    private BigDecimal medianSalary;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private List<MonthlySalaryCostDTO> monthlyCosts;
    private List<DepartmentSalaryCostDTO> departmentCosts;
    private Map<String, BigDecimal> costByContractType;
}
