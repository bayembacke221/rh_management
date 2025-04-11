package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List; /**
 * DTO pour le rapport de turnover
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnoverReportDTO {
    private Integer year;
    private Integer startEmployeeCount;
    private Integer endEmployeeCount;
    private Integer newHires;
    private Integer departures;
    private Double turnoverRate; // en pourcentage
    private List<MonthlyTurnoverDTO> monthlyTurnover;
    private List<DepartmentTurnoverDTO> departmentTurnover;
}
