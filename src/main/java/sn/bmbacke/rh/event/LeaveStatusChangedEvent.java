package sn.bmbacke.rh.event;

import lombok.Builder;
import lombok.Data;
import sn.bmbacke.rh.entity.Leave;
import sn.bmbacke.rh.entity.enums.LeaveStatus;

import java.time.LocalDateTime;

/**
 * Événement déclenché lors de la mise à jour du statut d'une demande de congé
 */
@Data
@Builder
public class LeaveStatusChangedEvent implements SystemEvent {
    private final Long leaveId;
    private final Long employeeId;
    private final Long managerId;
    private final LeaveStatus oldStatus;
    private final LeaveStatus newStatus;
    private final LocalDateTime eventTime;
    private final Long triggeredBy;

    public static LeaveStatusChangedEvent fromLeave(Leave leave, LeaveStatus oldStatus, Long triggeredBy) {
        return LeaveStatusChangedEvent.builder()
                .leaveId(leave.getId())
                .employeeId(leave.getEmployee().getId())
                .managerId(leave.getApprovedBy() != null ? leave.getApprovedBy().getId() : null)
                .oldStatus(oldStatus)
                .newStatus(leave.getStatus())
                .eventTime(LocalDateTime.now())
                .triggeredBy(triggeredBy)
                .build();
    }

    @Override
    public String getEventType() {
        return "LEAVE_STATUS_CHANGED";
    }
}