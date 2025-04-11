package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.bmbacke.rh.entity.enums.LeaveType;

/**
 * DTO pour les soldes de congés
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceDTO {
    private Long id;
    private EmployeeShortDTO employee;
    private LeaveType leaveType;
    private Integer year;
    private Float initialBalance;
    private Float usedBalance;
    private Float adjustedBalance;
    private String adjustmentReason;
    private Float currentBalance;
}
