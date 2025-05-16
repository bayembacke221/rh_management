package sn.bmbacke.rh.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration WebSocket pour les notifications en temps réel
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Préfixe pour les destinations qui seront mappées vers les méthodes @MessageMapping
        config.setApplicationDestinationPrefixes("/app");

        // Préfixe pour les canaux de diffusion (broadcast)
        config.enableSimpleBroker("/topic", "/queue", "/user");

        // Prefix pour les destinations utilisateur
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Point d'entrée WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:4200", "https://rh-management-ui.vercel.app")
                .withSockJS();
    }
}