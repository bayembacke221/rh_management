package sn.bmbacke.rh.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import sn.bmbacke.rh.entity.Employee;
import sn.bmbacke.rh.entity.Departement;
import sn.bmbacke.rh.entity.enums.NotificationPriority;
import sn.bmbacke.rh.entity.enums.NotificationType;
import sn.bmbacke.rh.event.LeaveStatusChangedEvent;
import sn.bmbacke.rh.payload.dto.NotificationCreateDTO;
import sn.bmbacke.rh.repository.EmployeeRepository;
import sn.bmbacke.rh.repository.DepartmentRepository;
import sn.bmbacke.rh.service.NotificationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Listener pour traiter les événements liés aux congés
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveEventListener {

    private final NotificationService notificationService;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Traite les événements de changement de statut de congé
     */
    @Async
    @EventListener
    public void handleLeaveStatusChangedEvent(LeaveStatusChangedEvent event) {
        log.info("Traitement de l'événement LeaveStatusChangedEvent: {}", event);

        try {
            switch (event.getNewStatus()) {
                case PENDING:
                    notifyPendingLeaveRequest(event);
                    break;
                case APPROVED:
                    notifyLeaveApproved(event);
                    break;
                case REJECTED:
                    notifyLeaveRejected(event);
                    break;
                case CANCELLED:
                    notifyLeaveCancelled(event);
                    break;
                default:
                    // Aucune notification pour les autres statuts
                    break;
            }
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'événement LeaveStatusChangedEvent", e);
        }
    }

    /**
     * Notifie le manager qu'une demande de congé est en attente
     */
    private void notifyPendingLeaveRequest(LeaveStatusChangedEvent event) {
        // Récupérer l'employé qui a demandé le congé
        Employee employee = employeeRepository.findById(event.getEmployeeId())
                .orElse(null);

        if (employee == null || employee.getManager() == null) {
            return;
        }

        // Créer une notification pour le manager
        NotificationCreateDTO notification = NotificationCreateDTO.builder()
                .title("Nouvelle demande de congé")
                .content("L'employé " + employee.getFirstName() + " " + employee.getLastName() + " a soumis une demande de congé.")
                .type(NotificationType.LEAVE_REQUEST)
                .priority(NotificationPriority.MEDIUM)
                .sourceType("LEAVE")
                .sourceId(event.getLeaveId())
                .actionUrl("/leaves/" + event.getLeaveId())
                .systemWide(false)
                .recipientIds(Collections.singletonList(employee.getManager().getId()))
                .expirationDate(LocalDateTime.now().plusDays(30))
                .build();

        notificationService.createNotification(notification);

        // Notification pour le DRH si le département existe
        if (employee.getDepartement() != null) {
            // Trouver les employés RH
            List<Employee> hrEmployees = getHREmployees();

            if (!hrEmployees.isEmpty()) {
                List<Long> hrIds = hrEmployees.stream()
                        .map(Employee::getId)
                        .toList();

                NotificationCreateDTO hrNotification = NotificationCreateDTO.builder()
                        .title("Nouvelle demande de congé à surveiller")
                        .content("L'employé " + employee.getFirstName() + " " + employee.getLastName() +
                                " du département " + employee.getDepartement().getName() +
                                " a soumis une demande de congé.")
                        .type(NotificationType.LEAVE_REQUEST)
                        .priority(NotificationPriority.LOW)
                        .sourceType("LEAVE")
                        .sourceId(event.getLeaveId())
                        .actionUrl("/leaves/" + event.getLeaveId())
                        .systemWide(false)
                        .recipientIds(hrIds)
                        .expirationDate(LocalDateTime.now().plusDays(30))
                        .build();

                notificationService.createNotification(hrNotification);
            }
        }
    }

    /**
     * Notifie l'employé que sa demande de congé a été approuvée
     */
    private void notifyLeaveApproved(LeaveStatusChangedEvent event) {
        // Récupérer l'employé qui a demandé le congé
        Employee employee = employeeRepository.findById(event.getEmployeeId())
                .orElse(null);

        if (employee == null) {
            return;
        }

        String managerName = "Le responsable";
        if (event.getManagerId() != null) {
            Employee manager = employeeRepository.findById(event.getManagerId()).orElse(null);
            if (manager != null) {
                managerName = manager.getFirstName() + " " + manager.getLastName();
            }
        }

        // Créer une notification pour l'employé
        NotificationCreateDTO notification = NotificationCreateDTO.builder()
                .title("Demande de congé approuvée")
                .content(managerName + " a approuvé votre demande de congé.")
                .type(NotificationType.LEAVE_APPROVED)
                .priority(NotificationPriority.MEDIUM)
                .sourceType("LEAVE")
                .sourceId(event.getLeaveId())
                .actionUrl("/leaves/" + event.getLeaveId())
                .systemWide(false)
                .recipientIds(Collections.singletonList(event.getEmployeeId()))
                .expirationDate(LocalDateTime.now().plusDays(30))
                .build();

        notificationService.createNotification(notification);
    }

    /**
     * Notifie l'employé que sa demande de congé a été rejetée
     */
    private void notifyLeaveRejected(LeaveStatusChangedEvent event) {
        // Récupérer l'employé qui a demandé le congé
        Employee employee = employeeRepository.findById(event.getEmployeeId())
                .orElse(null);

        if (employee == null) {
            return;
        }

        String managerName = "Le responsable";
        if (event.getManagerId() != null) {
            Employee manager = employeeRepository.findById(event.getManagerId()).orElse(null);
            if (manager != null) {
                managerName = manager.getFirstName() + " " + manager.getLastName();
            }
        }

        // Créer une notification pour l'employé
        NotificationCreateDTO notification = NotificationCreateDTO.builder()
                .title("Demande de congé rejetée")
                .content(managerName + " a rejeté votre demande de congé. Veuillez consulter les détails pour plus d'informations.")
                .type(NotificationType.LEAVE_REJECTED)
                .priority(NotificationPriority.HIGH)
                .sourceType("LEAVE")
                .sourceId(event.getLeaveId())
                .actionUrl("/leaves/" + event.getLeaveId())
                .systemWide(false)
                .recipientIds(Collections.singletonList(event.getEmployeeId()))
                .expirationDate(LocalDateTime.now().plusDays(30))
                .build();

        notificationService.createNotification(notification);
    }

    /**
     * Notifie le manager qu'une demande de congé a été annulée
     */
    private void notifyLeaveCancelled(LeaveStatusChangedEvent event) {
        // Récupérer l'employé qui a demandé le congé
        Employee employee = employeeRepository.findById(event.getEmployeeId())
                .orElse(null);

        if (employee == null || employee.getManager() == null) {
            return;
        }

        // Créer une notification pour le manager
        NotificationCreateDTO notification = NotificationCreateDTO.builder()
                .title("Demande de congé annulée")
                .content("L'employé " + employee.getFirstName() + " " + employee.getLastName() + " a annulé sa demande de congé.")
                .type(NotificationType.LEAVE_REQUEST)
                .priority(NotificationPriority.LOW)
                .sourceType("LEAVE")
                .sourceId(event.getLeaveId())
                .actionUrl("/leaves/" + event.getLeaveId())
                .systemWide(false)
                .recipientIds(Collections.singletonList(employee.getManager().getId()))
                .expirationDate(LocalDateTime.now().plusDays(15))
                .build();

        notificationService.createNotification(notification);
    }

    /**
     * Récupère la liste des employés RH
     */
    private List<Employee> getHREmployees() {
        // Cette méthode devrait être adaptée selon votre logique métier
        // Par exemple, vous pourriez avoir un département RH identifié
        List<Employee> hrEmployees = new ArrayList<>();

        try {
            // Rechercher le département RH (exemple : par nom ou par code)
            Departement hrDepartment = departmentRepository.findByCode("RH");

            if (hrDepartment != null) {
                // Récupérer tous les employés de ce département
                hrEmployees = employeeRepository.findByDepartement_Id(hrDepartment.getId(), Pageable.unpaged())
                        .getContent();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des employés RH", e);
        }

        return hrEmployees;
    }
}