package sn.bmbacke.rh.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.bmbacke.rh.entity.Employee;
import sn.bmbacke.rh.entity.Notification;
import sn.bmbacke.rh.entity.enums.NotificationType;
import sn.bmbacke.rh.exception.ResourceNotFoundException;
import sn.bmbacke.rh.payload.dto.NotificationCreateDTO;
import sn.bmbacke.rh.payload.dto.NotificationDTO;
import sn.bmbacke.rh.payload.mapper.NotificationMapper;
import sn.bmbacke.rh.repository.EmployeeRepository;
import sn.bmbacke.rh.repository.NotificationRepository;
import sn.bmbacke.rh.service.NotificationService;
import sn.bmbacke.rh.websocket.NotificationWebSocketService;
import sn.bmbacke.rh.websocket.WebSocketNotificationDTO;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationWebSocketService webSocketService;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotificationsForEmployee(Long employeeId, Pageable pageable) {
        // Vérifier que l'employé existe
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId));

        // Récupérer les notifications pour cet employé
        Page<Notification> notifications = notificationRepository.findActiveNotificationsForEmployee(
                employeeId, LocalDateTime.now(), pageable);

        // Convertir en DTOs en incluant le statut de lecture
        return notifications.map(notification ->
                notificationMapper.toDtoWithReadStatus(notification, employee));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUnreadNotificationsForEmployee(Long employeeId, Pageable pageable) {
        // Vérifier que l'employé existe
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId));

        // Récupérer les notifications non lues
        Page<Notification> notifications = notificationRepository.findUnreadNotificationsForEmployee(
                employeeId, LocalDateTime.now(), pageable);

        // Convertir en DTOs (ces notifications sont non lues par définition)
        return notifications.map(notification -> {
            NotificationDTO dto = notificationMapper.toDtoWithReadStatus(notification, employee);
            dto.setRead(false);
            return dto;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnreadNotificationsForEmployee(Long employeeId) {
        // Vérifier que l'employé existe
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId);
        }

        return notificationRepository.countUnreadNotificationsForEmployee(
                employeeId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotificationsForEmployeeByType(Long employeeId, NotificationType type, Pageable pageable) {
        // Vérifier que l'employé existe
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId));

        // Récupérer les notifications par type
        Page<Notification> notifications = notificationRepository.findNotificationsForEmployeeByType(
                employeeId, type, LocalDateTime.now(), pageable);

        // Convertir en DTOs
        return notifications.map(notification ->
                notificationMapper.toDtoWithReadStatus(notification, employee));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationType> getNotificationTypesForEmployee(Long employeeId) {
        // Vérifier que l'employé existe
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId);
        }

        return notificationRepository.findNotificationTypesForEmployee(
                employeeId, LocalDateTime.now());
    }

    @Override
    public NotificationDTO createNotification(NotificationCreateDTO createDTO) {
        // Validation de base
        if (createDTO.getExpirationDate() == null) {
            // Par défaut, expiration après 30 jours
            createDTO.setExpirationDate(LocalDateTime.now().plusDays(30));
        }

        // Création de la notification
        Notification notification = notificationMapper.toEntity(createDTO);

        // Sauvegarder la notification
        Notification savedNotification = notificationRepository.save(notification);

        // Convertir en DTO pour la réponse
        NotificationDTO notificationDTO = notificationMapper.toDto(savedNotification);

        // Envoyer la notification via WebSocket
        WebSocketNotificationDTO webSocketDTO = webSocketService.convertToWebSocketDTO(savedNotification);

        if (createDTO.isSystemWide()) {
            // Si c'est une notification système, la diffuser à tous les utilisateurs
            webSocketService.broadcastNotification(webSocketDTO);
        } else if (createDTO.getRecipientIds() != null && !createDTO.getRecipientIds().isEmpty()) {
            // Sinon, l'envoyer à chaque destinataire
            for (Long employeeId : createDTO.getRecipientIds()) {
                // Récupérer l'ID de l'utilisateur correspondant à cet employé
                employeeRepository.findById(employeeId).ifPresent(employee -> {
                    if (employee.getUser() != null) {
                        webSocketService.sendNotificationToUser(employee.getUser().getId(), webSocketDTO);

                        // Mettre à jour le compteur de notifications non lues
                        Long unreadCount = countUnreadNotificationsForEmployee(employeeId);
                        webSocketService.sendUnreadCountToUser(employee.getUser().getId(), unreadCount);
                    }
                });
            }
        }

        return notificationDTO;
    }

    @Override
    public NotificationDTO markNotificationAsRead(Long notificationId, Long employeeId) {
        // Récupérer la notification
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée avec l'ID : " + notificationId));

        // Récupérer l'employé
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId));

        // Vérifier que l'employé est bien destinataire de cette notification
        if (!notification.getRecipients().contains(employee) && !notification.isSystemWide()) {
            throw new ResourceNotFoundException("L'employé n'est pas destinataire de cette notification");
        }

        // Marquer comme lu
        notification.markAsRead(employee);
        Notification updatedNotification = notificationRepository.save(notification);

        // Convertir en DTO
        NotificationDTO dto = notificationMapper.toDtoWithReadStatus(updatedNotification, employee);
        dto.setRead(true);

        // Mettre à jour le compteur de notifications non lues via WebSocket
        if (employee.getUser() != null) {
            Long unreadCount = countUnreadNotificationsForEmployee(employeeId);
            webSocketService.sendUnreadCountToUser(employee.getUser().getId(), unreadCount);
        }

        return dto;
    }

    @Override
    public void markAllNotificationsAsRead(Long employeeId) {
        // Récupérer l'employé
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employé non trouvé avec l'ID : " + employeeId));

        // Récupérer toutes les notifications non lues
        Page<Notification> unreadNotifications = notificationRepository.findUnreadNotificationsForEmployee(
                employeeId, LocalDateTime.now(), Pageable.unpaged());

        // Marquer chaque notification comme lue
        unreadNotifications.forEach(notification -> {
            notification.markAsRead(employee);
            notificationRepository.save(notification);
        });

        // Mettre à jour le compteur de notifications non lues via WebSocket
        if (employee.getUser() != null) {
            webSocketService.sendUnreadCountToUser(employee.getUser().getId(), 0L);
        }
    }

    @Override
    public void deactivateNotification(Long notificationId) {
        // Récupérer la notification
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée avec l'ID : " + notificationId));

        // Désactiver la notification
        notification.setActive(false);
        notificationRepository.save(notification);

    }

    @Override
    public void deactivateNotificationsForSource(String sourceType, Long sourceId) {
        // Récupérer toutes les notifications pour cette source
        List<Notification> notifications = notificationRepository.findBySourceTypeAndSourceId(sourceType, sourceId);

        // Désactiver chaque notification
        notifications.forEach(notification -> {
            notification.setActive(false);
            notificationRepository.save(notification);
        });

    }
}