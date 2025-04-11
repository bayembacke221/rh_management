package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour obtenir un résumé des congés d'un employé
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveEmployeeSummaryDTO {
    private EmployeeShortDTO employee;
    private Integer totalLeavesThisYear;
    private Integer pendingLeaves;
    private Float annualLeaveBalance;
    private Float sickLeaveBalance;
    private List<LeaveDTO> upcomingLeaves;
}
