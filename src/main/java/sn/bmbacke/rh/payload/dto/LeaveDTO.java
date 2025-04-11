package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.bmbacke.rh.entity.enums.LeaveStatus;
import sn.bmbacke.rh.entity.enums.LeaveType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO pour les demandes de congés
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveDTO {
    private Long id;
    private EmployeeShortDTO employee;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType leaveType;
    private LeaveStatus status;
    private String reason;
    private String comments;
    private EmployeeShortDTO approvedBy;
    private LocalDateTime approvalDate;
    private String rejectionReason;
    private Integer durationDays;
    private Boolean halfDay;
    private String attachments;
}
