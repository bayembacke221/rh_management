package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; /**
 * DTO pour les coûts salariaux mensuels
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySalaryCostDTO {
    private Integer year;
    private Integer month;
    private String monthName;
    private BigDecimal totalCost;
    private Integer employeeCount;
    private BigDecimal averageSalary;
}
