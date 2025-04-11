package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.bmbacke.rh.entity.enums.LeaveType;

/**
 * DTO pour la création d'une politique de congés
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicyCreateDTO {
    private LeaveType leaveType;
    private Integer year;
    private Float defaultDays;
    private Integer maxConsecutiveDays;
    private Integer minRequestNoticeDays;
    private Boolean requiresApproval;
    private Boolean requiresDocumentation;
    private Boolean isPaid;
    private Boolean carryForwardAllowed;
    private Float maxCarryForwardDays;
    private String description;
}
