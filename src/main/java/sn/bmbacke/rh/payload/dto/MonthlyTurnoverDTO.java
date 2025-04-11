package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * DTO pour le turnover mensuel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTurnoverDTO {
    private Integer year;
    private Integer month;
    private String monthName;
    private Integer newHires;
    private Integer departures;
    private Double turnoverRate; // en pourcentage
}
