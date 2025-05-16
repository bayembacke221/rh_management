package sn.bmbacke.rh.payload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.bmbacke.rh.entity.enums.NotificationPriority;
import sn.bmbacke.rh.entity.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private String title;
    private String content;
    private NotificationType type;
    private NotificationPriority priority;
    private String sourceType;
    private Long sourceId;
    private String actionUrl;
    private boolean systemWide;
    private List<EmployeeShortDTO> recipients;
    private boolean read;
    private LocalDateTime createdDate;
    private LocalDateTime expirationDate;
    private boolean active;
}