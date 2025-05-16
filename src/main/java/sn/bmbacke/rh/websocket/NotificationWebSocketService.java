package sn.bmbacke.rh.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import sn.bmbacke.rh.entity.Notification;
import sn.bmbacke.rh.repository.UserRepository;

/**
 * Service pour envoyer des notifications via WebSocket
 */
@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    /**
     * Envoie une notification à un utilisateur spécifique
     */
    public void sendNotificationToUser(Long userId, WebSocketNotificationDTO notification) {
        // Récupérer l'utilisateur
        userRepository.findById(userId).ifPresent(user -> {
            // Envoyer la notification au canal spécifique de l'utilisateur
            messagingTemplate.convertAndSendToUser(
                    user.getUsername(),
                    "/queue/notifications",
                    notification
            );
        });
    }

    /**
     * Envoie une notification à tous les utilisateurs
     */
    public void broadcastNotification(WebSocketNotificationDTO notification) {
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    /**
     * Envoie une mise à jour du compteur de notifications non lues à un utilisateur
     */
    public void sendUnreadCountToUser(Long userId, Long count) {
        userRepository.findById(userId).ifPresent(user -> {
            messagingTemplate.convertAndSendToUser(
                    user.getUsername(),
                    "/queue/notifications/count",
                    count
            );
        });
    }

    /**
     * Convertit une notification en DTO WebSocket
     */
    public WebSocketNotificationDTO convertToWebSocketDTO(Notification notification) {
        return WebSocketNotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .priority(notification.getPriority())
                .sourceType(notification.getSourceType())
                .sourceId(notification.getSourceId())
                .actionUrl(notification.getActionUrl())
                .createdDate(notification.getCreatedDate())
                .read(false)
                .build();
    }
}