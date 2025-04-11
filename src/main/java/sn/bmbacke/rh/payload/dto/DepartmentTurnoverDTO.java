package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * DTO pour le turnover par département
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentTurnoverDTO {
    private Long departmentId;
    private String departmentName;
    private Integer startEmployeeCount;
    private Integer endEmployeeCount;
    private Integer newHires;
    private Integer departures;
    private Double turnoverRate; // en pourcentage
}
