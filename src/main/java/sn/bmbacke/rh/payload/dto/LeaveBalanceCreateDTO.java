package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.bmbacke.rh.entity.enums.LeaveType;

/**
 * DTO pour la création d'un solde de congés
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceCreateDTO {
    private Long employeeId;
    private LeaveType leaveType;
    private Integer year;
    private Float initialBalance;
}
