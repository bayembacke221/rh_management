package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; /**
 * DTO pour les coûts salariaux par département
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentSalaryCostDTO {
    private Long departmentId;
    private String departmentName;
    private BigDecimal totalCost;
    private Integer employeeCount;
    private BigDecimal averageSalary;
    private Double percentageOfTotal; // en pourcentage
}
