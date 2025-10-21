package co.edu.uniquindio.application.config;

import co.edu.uniquindio.application.config.ws.CustomHandshakeHandler;
import co.edu.uniquindio.application.config.ws.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")
                .setHandshakeHandler(new CustomHandshakeHandler())
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOrigins("http://localhost:4200", "http://localhost:8080", "http://localhost:63342") // Punto final del WebSocket
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue"); // Prefijo para los mensajes enviados al cliente
        registry.setApplicationDestinationPrefixes("/app"); // Prefijo para mensajes enviados desde el cliente al servidor
        registry.setUserDestinationPrefix("/user"); // Prefijo para mensajes dirigidos a usuarios específicos
    }
}
