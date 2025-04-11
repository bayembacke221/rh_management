package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map; /**
 * DTO pour la distribution par genre
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenderDistributionDTO {
    private Integer maleCount;
    private Integer femaleCount;
    private Double malePercentage;
    private Double femalePercentage;
    private Map<String, GenderDistributionDTO> byDepartment;
    private Map<String, GenderDistributionDTO> byPosition;
}
