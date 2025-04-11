package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map; /**
 * DTO pour la distribution par âge
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgeDistributionDTO {
    private Double averageAge;
    private Integer under25Count;
    private Integer age25to34Count;
    private Integer age35to44Count;
    private Integer age45to54Count;
    private Integer age55plusCount;
    private Map<String, Double> averageAgeByDepartment;
}
