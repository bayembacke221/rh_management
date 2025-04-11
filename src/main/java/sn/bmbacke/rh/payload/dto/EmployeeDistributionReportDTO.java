package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.bmbacke.rh.entity.enums.Status;
import sn.bmbacke.rh.entity.enums.Type;

import java.util.Map; /**
 * DTO pour la répartition des employés
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDistributionReportDTO {
    private Integer totalEmployees;
    private Map<Status, Integer> statusDistribution;
    private Map<Type, Integer> contractTypeDistribution;
    private Map<String, Integer> departmentDistribution;
    private Map<String, Integer> positionDistribution;
    private GenderDistributionDTO genderDistribution;
    private AgeDistributionDTO ageDistribution;
    private SeniorityDistributionDTO seniorityDistribution;
}
