package sn.bmbacke.rh.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Service pour publier des événements système
 */
@Component
@RequiredArgsConstructor
public class SystemEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publie un événement système
     */
    public void publishEvent(SystemEvent event) {
        eventPublisher.publishEvent(event);
    }
}