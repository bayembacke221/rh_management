package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.bmbacke.rh.entity.enums.LeaveType; /**
 * DTO pour l'absentéisme par type de congé
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveTypeAbsenteeismDTO {
    private LeaveType leaveType;
    private String leaveTypeName;
    private Integer absenceDays;
    private Double percentageOfTotal; // en pourcentage
}
