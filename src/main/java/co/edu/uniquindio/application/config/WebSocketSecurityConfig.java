package co.edu.uniquindio.application.config;

import co.edu.uniquindio.application.security.JWTUtils;
import co.edu.uniquindio.application.services.UsuarioDetallesServicio;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JWTUtils jwtUtils;
    private final UsuarioDetallesServicio usuarioDetallesServicio;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    List<String> authHeaders = accessor.getNativeHeader("Authorization");
                    
                    if (authHeaders != null && !authHeaders.isEmpty()) {
                        String authHeader = authHeaders.get(0);
                        
                        if (authHeader.startsWith("Bearer ")) {
                            String token = authHeader.substring(7);
                            
                            try {
                                String username = jwtUtils.decodificarJwt(token).getPayload().getSubject();
                                
                                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                                    UserDetails userDetails = usuarioDetallesServicio.cargarUsuarioPorUsername(username);
                                    
                                    if (jwtUtils.validarToken(token, userDetails)) {
                                        UsernamePasswordAuthenticationToken authToken = 
                                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                                        SecurityContextHolder.getContext().setAuthentication(authToken);
                                        accessor.setUser(authToken);
                                    }
                                }
                            } catch (Exception e) {
                                // Token inválido, rechazar conexión
                                return null;
                            }
                        }
                    }
                }
                
                return message;
            }
        });
    }
}
