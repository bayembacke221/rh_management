package sn.bmbacke.rh.event;

import java.time.LocalDateTime;

/**
 * Interface pour tous les événements du système
 */
public interface SystemEvent {
    /**
     * Retourne le type d'événement
     */
    String getEventType();

    /**
     * Retourne la date et l'heure de l'événement
     */
    LocalDateTime getEventTime();

    /**
     * Retourne l'ID de l'utilisateur qui a déclenché l'événement
     */
    Long getTriggeredBy();
}