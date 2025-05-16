package sn.bmbacke.rh.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import sn.bmbacke.rh.entity.Employee;
import sn.bmbacke.rh.entity.enums.NotificationPriority;
import sn.bmbacke.rh.entity.enums.NotificationType;
import sn.bmbacke.rh.event.DocumentUploadedEvent;
import sn.bmbacke.rh.payload.dto.NotificationCreateDTO;
import sn.bmbacke.rh.repository.EmployeeRepository;
import sn.bmbacke.rh.service.NotificationService;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Listener pour traiter les événements liés aux documents
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentEventListener {

    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository;

    /**
     * Traite les événements de téléchargement de document
     */
    @Async
    @EventListener
    public void handleDocumentUploadedEvent(DocumentUploadedEvent event) {
        log.info("Traitement de l'événement DocumentUploadedEvent: {}", event);

        try {
            // Notifier l'employé concerné
            if (event.getEmployeeId() != null) {
                notifyDocumentUploaded(event);
            }
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'événement DocumentUploadedEvent", e);
        }
    }

    /**
     * Notifie l'employé qu'un document a été téléchargé pour lui
     */
    private void notifyDocumentUploaded(DocumentUploadedEvent event) {
        // Vérifier si l'émetteur et le destinataire sont différents
        if (event.getTriggeredBy().equals(event.getEmployeeId())) {
            // L'employé a téléchargé son propre document, pas besoin de notification
            return;
        }

        // Récupérer l'employé concerné
        Employee employee = employeeRepository.findById(event.getEmployeeId())
                .orElse(null);

        if (employee == null) {
            return;
        }

        // Récupérer l'émetteur (personne qui a téléchargé le document)
        Employee uploader = employeeRepository.findById(event.getTriggeredBy())
                .orElse(null);

        String uploaderName = uploader != null
                ? uploader.getFirstName() + " " + uploader.getLastName()
                : "Un administrateur";

        // Créer une notification pour l'employé
        NotificationCreateDTO notification = NotificationCreateDTO.builder()
                .title("Nouveau document ajouté")
                .content(uploaderName + " a ajouté un nouveau document (" + event.getDocumentName() + ") à votre dossier.")
                .type(NotificationType.DOCUMENT_UPLOADED)
                .priority(NotificationPriority.MEDIUM)
                .sourceType("DOCUMENT")
                .sourceId(event.getDocumentId())
                .actionUrl("/documents/" + event.getDocumentId())
                .systemWide(false)
                .recipientIds(Collections.singletonList(event.getEmployeeId()))
                .expirationDate(LocalDateTime.now().plusDays(30))
                .build();

        notificationService.createNotification(notification);

        // Si c'est un document important (à adapter selon vos besoins)
        if (isImportantDocument(event.getDocumentType())) {
            // Notifier également le manager de l'employé
            if (employee.getManager() != null) {
                NotificationCreateDTO managerNotification = NotificationCreateDTO.builder()
                        .title("Document important ajouté")
                        .content("Un document important (" + event.getDocumentName() + ") a été ajouté au dossier de "
                                + employee.getFirstName() + " " + employee.getLastName() + ".")
                        .type(NotificationType.DOCUMENT_UPLOADED)
                        .priority(NotificationPriority.MEDIUM)
                        .sourceType("DOCUMENT")
                        .sourceId(event.getDocumentId())
                        .actionUrl("/documents/" + event.getDocumentId())
                        .systemWide(false)
                        .recipientIds(Collections.singletonList(employee.getManager().getId()))
                        .expirationDate(LocalDateTime.now().plusDays(15))
                        .build();

                notificationService.createNotification(managerNotification);
            }
        }
    }

    /**
     * Détermine si un type de document est considéré comme important
     */
    private boolean isImportantDocument(String documentType) {
        // À adapter selon vos besoins et vos types de documents
        return documentType != null && (
                documentType.equals("CONTRACT") ||
                        documentType.equals("IDENTITY") ||
                        documentType.equals("DIPLOMA")
        );
    }
}