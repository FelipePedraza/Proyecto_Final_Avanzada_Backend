package co.edu.uniquindio.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilitar un broker simple en memoria para mensajes
        config.enableSimpleBroker("/topic", "/queue");
        // Prefijo para mensajes que van al servidor
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint para conectar WebSocket con CORS configurado
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setAllowedOrigins("http://localhost:63342", "http://localhost:4200")
                .withSockJS();
    }
}
