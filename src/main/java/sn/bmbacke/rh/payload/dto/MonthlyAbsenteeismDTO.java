package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * DTO pour l'absentéisme mensuel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyAbsenteeismDTO {
    private Integer year;
    private Integer month;
    private String monthName;
    private Integer totalEmployees;
    private Integer workingDays;
    private Integer absenceDays;
    private Double absenteeismRate; // en pourcentage
}
