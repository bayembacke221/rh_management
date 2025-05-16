package sn.bmbacke.rh.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import sn.bmbacke.rh.entity.Employee;
import sn.bmbacke.rh.entity.enums.NotificationPriority;
import sn.bmbacke.rh.entity.enums.NotificationType;
import sn.bmbacke.rh.event.GenericNotificationEvent;
import sn.bmbacke.rh.payload.dto.NotificationCreateDTO;
import sn.bmbacke.rh.repository.EmployeeRepository;
import sn.bmbacke.rh.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Listener pour traiter les événements de notification génériques
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenericNotificationListener {

    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository;

    /**
     * Traite les événements de notification génériques
     */
    @Async
    @EventListener
    public void handleGenericNotificationEvent(GenericNotificationEvent event) {
        log.info("Traitement de l'événement GenericNotificationEvent: {}", event);

        try {
            // Convertir le type de notification
            NotificationType notificationType = convertStringToNotificationType(event.getNotificationType());

            // Convertir la priorité
            NotificationPriority priority = convertStringToNotificationPriority(event.getPriorityLevel());

            // Déterminer les destinataires
            List<Long> recipientIds = determineRecipients(event);

            // Déterminer la date d'expiration
            LocalDateTime expirationDate = determineExpirationDate(event);

            // Créer la notification
            NotificationCreateDTO notification = NotificationCreateDTO.builder()
                    .title(event.getTitle())
                    .content(event.getContent())
                    .type(notificationType)
                    .priority(priority)
                    .sourceType(event.getSourceType())
                    .sourceId(event.getSourceId())
                    .actionUrl(null) // À définir si besoin
                    .systemWide(event.getSystemWide() != null && event.getSystemWide())
                    .recipientIds(recipientIds)
                    .expirationDate(expirationDate)
                    .build();

            notificationService.createNotification(notification);
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'événement GenericNotificationEvent", e);
        }
    }

    /**
     * Convertit une chaîne en NotificationType
     */
    private NotificationType convertStringToNotificationType(String typeString) {
        if (typeString == null || typeString.isEmpty()) {
            return NotificationType.SYSTEM;
        }

        try {
            return NotificationType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Type de notification inconnu: {}, utilisation de SYSTEM par défaut", typeString);
            return NotificationType.SYSTEM;
        }
    }

    /**
     * Convertit une chaîne en NotificationPriority
     */
    private NotificationPriority convertStringToNotificationPriority(String priorityString) {
        if (priorityString == null || priorityString.isEmpty()) {
            return NotificationPriority.MEDIUM;
        }

        try {
            return NotificationPriority.valueOf(priorityString.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Priorité de notification inconnue: {}, utilisation de MEDIUM par défaut", priorityString);
            return NotificationPriority.MEDIUM;
        }
    }

    /**
     * Détermine la liste des destinataires
     */
    private List<Long> determineRecipients(GenericNotificationEvent event) {
        // Si des destinataires spécifiques sont fournis
        if (event.getRecipientIds() != null && event.getRecipientIds().length > 0) {
            return Arrays.asList(event.getRecipientIds());
        }

        // Si l'événement est destiné à tous les employés
        if (event.getSystemWide() != null && event.getSystemWide()) {
            return Collections.emptyList();
        }

        // Sinon, on récupère les employés concernés
        return Collections.emptyList();
    }

    /**
     * Détermine la date d'expiration de la notification
     */
    private LocalDateTime determineExpirationDate(GenericNotificationEvent event) {
        // Si une durée en jours est spécifiée, l'utiliser
        if (event.getExpirationDays() != null && event.getExpirationDays() > 0) {
            return LocalDateTime.now().plusDays(event.getExpirationDays());
        }

        // Sinon, expiration par défaut après 30 jours
        return LocalDateTime.now().plusDays(30);
    }
}