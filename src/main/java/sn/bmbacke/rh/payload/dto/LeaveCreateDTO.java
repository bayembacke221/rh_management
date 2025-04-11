package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.bmbacke.rh.entity.enums.LeaveType;

import java.time.LocalDate;

/**
 * DTO pour la création d'une demande de congé
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCreateDTO {
    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType leaveType;
    private String reason;
    private Boolean halfDay;
    private String attachments;
}

