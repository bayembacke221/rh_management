package sn.bmbacke.rh.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.bmbacke.rh.entity.User;
import sn.bmbacke.rh.entity.enums.NotificationType;
import sn.bmbacke.rh.payload.dto.NotificationCreateDTO;
import sn.bmbacke.rh.payload.dto.NotificationDTO;
import sn.bmbacke.rh.service.NotificationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "API pour la gestion des notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Récupérer les notifications de l'utilisateur connecté",
            description = "Retourne une liste paginée des notifications pour l'utilisateur connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications récupérées avec succès",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    public ResponseEntity<Page<NotificationDTO>> getMyNotifications(
            @PageableDefault(size = 20, sort = "createdDate") Pageable pageable,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getNotificationsForEmployee(
                user.getEmployee().getId(), pageable));
    }

    @GetMapping("/unread")
    @Operation(summary = "Récupérer les notifications non lues",
            description = "Retourne une liste paginée des notifications non lues pour l'utilisateur connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications récupérées avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    public ResponseEntity<Page<NotificationDTO>> getUnreadNotifications(
            @PageableDefault(size = 20, sort = "createdDate") Pageable pageable,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getUnreadNotificationsForEmployee(
                user.getEmployee().getId(), pageable));
    }

    @GetMapping("/count-unread")
    @Operation(summary = "Compter les notifications non lues",
            description = "Retourne le nombre de notifications non lues pour l'utilisateur connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comptage effectué avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    public ResponseEntity<Map<String, Long>> countUnreadNotifications(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Long count = notificationService.countUnreadNotificationsForEmployee(
                user.getEmployee().getId());

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/types")
    @Operation(summary = "Récupérer les types de notifications disponibles",
            description = "Retourne la liste des types de notifications qui existent pour l'utilisateur connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Types récupérés avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    public ResponseEntity<List<NotificationType>> getNotificationTypes(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getNotificationTypesForEmployee(
                user.getEmployee().getId()));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Récupérer les notifications par type",
            description = "Retourne une liste paginée des notifications d'un type spécifique pour l'utilisateur connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications récupérées avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    public ResponseEntity<Page<NotificationDTO>> getNotificationsByType(
            @Parameter(description = "Type de notification") @PathVariable NotificationType type,
            @PageableDefault(size = 20, sort = "createdDate") Pageable pageable,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getNotificationsForEmployeeByType(
                user.getEmployee().getId(), type, pageable));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Marquer une notification comme lue",
            description = "Marque une notification spécifique comme lue pour l'utilisateur connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marquée comme lue avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content),
            @ApiResponse(responseCode = "404", description = "Notification non trouvée", content = @Content)
    })
    public ResponseEntity<NotificationDTO> markAsRead(
            @Parameter(description = "ID de la notification") @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.markNotificationAsRead(id, user.getEmployee().getId()));
    }

    @PostMapping("/mark-all-read")
    @Operation(summary = "Marquer toutes les notifications comme lues",
            description = "Marque toutes les notifications non lues comme lues pour l'utilisateur connecté")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notifications marquées comme lues avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        notificationService.markAllNotificationsAsRead(user.getEmployee().getId());
        return ResponseEntity.noContent().build();
    }

    // Endpoint pour les administrateurs uniquement
    @PostMapping
    @Operation(summary = "Créer une notification",
            description = "Crée une nouvelle notification (réservé aux administrateurs)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Requête invalide", content = @Content),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    public ResponseEntity<NotificationDTO> createNotification(
            @RequestBody NotificationCreateDTO notificationCreateDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.createNotification(notificationCreateDTO));
    }

    // Endpoint pour les administrateurs uniquement
    @DeleteMapping("/{id}")
    @Operation(summary = "Désactiver une notification",
            description = "Désactive une notification (réservé aux administrateurs)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notification désactivée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié", content = @Content),
            @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content),
            @ApiResponse(responseCode = "404", description = "Notification non trouvée", content = @Content)
    })
    public ResponseEntity<Void> deactivateNotification(
            @Parameter(description = "ID de la notification") @PathVariable Long id) {
        notificationService.deactivateNotification(id);
        return ResponseEntity.noContent().build();
    }
}