package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * DTO pour l'absentéisme par département
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentAbsenteeismDTO {
    private Long departmentId;
    private String departmentName;
    private Integer totalEmployees;
    private Integer absenceDays;
    private Double absenteeismRate; // en pourcentage
}
