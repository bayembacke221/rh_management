package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.bmbacke.rh.entity.enums.LeaveStatus;

/**
 * DTO pour l'approbation ou le rejet d'une demande de congé
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApprovalDTO {
    private LeaveStatus status;
    private String comments;
    private String rejectionReason;
}
