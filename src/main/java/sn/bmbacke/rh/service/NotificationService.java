package sn.bmbacke.rh.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sn.bmbacke.rh.entity.enums.NotificationType;
import sn.bmbacke.rh.payload.dto.NotificationCreateDTO;
import sn.bmbacke.rh.payload.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {

    /**
     * Récupère toutes les notifications pour un employé avec pagination
     */
    Page<NotificationDTO> getNotificationsForEmployee(Long employeeId, Pageable pageable);

    /**
     * Récupère les notifications non lues pour un employé avec pagination
     */
    Page<NotificationDTO> getUnreadNotificationsForEmployee(Long employeeId, Pageable pageable);

    /**
     * Compte le nombre de notifications non lues pour un employé
     */
    Long countUnreadNotificationsForEmployee(Long employeeId);

    /**
     * Récupère les notifications par type pour un employé avec pagination
     */
    Page<NotificationDTO> getNotificationsForEmployeeByType(Long employeeId, NotificationType type, Pageable pageable);

    /**
     * Récupère les types de notifications disponibles pour un employé
     */
    List<NotificationType> getNotificationTypesForEmployee(Long employeeId);

    /**
     * Crée une nouvelle notification
     */
    NotificationDTO createNotification(NotificationCreateDTO notificationCreateDTO);

    /**
     * Marque une notification comme lue pour un employé
     */
    NotificationDTO markNotificationAsRead(Long notificationId, Long employeeId);

    /**
     * Marque toutes les notifications comme lues pour un employé
     */
    void markAllNotificationsAsRead(Long employeeId);

    /**
     * Désactive une notification
     */
    void deactivateNotification(Long notificationId);

    /**
     * Désactive toutes les notifications liées à une source spécifique
     */
    void deactivateNotificationsForSource(String sourceType, Long sourceId);
}