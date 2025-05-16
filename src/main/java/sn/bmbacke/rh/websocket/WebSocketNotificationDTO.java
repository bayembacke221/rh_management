package sn.bmbacke.rh.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.bmbacke.rh.entity.enums.NotificationPriority;
import sn.bmbacke.rh.entity.enums.NotificationType;

import java.time.LocalDateTime;

/**
 * DTO pour envoyer une notification via WebSocket
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketNotificationDTO {
    private Long id;
    private String title;
    private String content;
    private NotificationType type;
    private NotificationPriority priority;
    private String sourceType;
    private Long sourceId;
    private String actionUrl;
    private LocalDateTime createdDate;
    private boolean read;
}