package sn.bmbacke.rh.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Événement système pour les notifications génériques
 */
@Data
@Builder
public class GenericNotificationEvent implements SystemEvent {
    private final String title;
    private final String content;
    private final String notificationType;
    private final String priorityLevel;
    private final LocalDateTime eventTime;
    private final Long triggeredBy;
    private final String sourceType;
    private final Long sourceId;
    private final Boolean systemWide;
    private final Long[] recipientIds;
    private final Integer expirationDays;

    @Override
    public String getEventType() {
        return "GENERIC_NOTIFICATION";
    }
}