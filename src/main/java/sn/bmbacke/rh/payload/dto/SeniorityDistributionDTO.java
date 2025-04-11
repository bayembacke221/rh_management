package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map; /**
 * DTO pour la distribution par ancienneté
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeniorityDistributionDTO {
    private Double averageSeniority; // en années
    private Integer lessThan1YearCount;
    private Integer oneToThreeYearsCount;
    private Integer threeToFiveYearsCount;
    private Integer fiveToTenYearsCount;
    private Integer moreThanTenYearsCount;
    private Map<String, Double> averageSeniorityByDepartment;
}
