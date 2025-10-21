package co.edu.uniquindio.application.config.ws;

import co.edu.uniquindio.application.security.JWTUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JWTUtils jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
            String idUsuario = getToken(httpServletRequest);

            if (idUsuario != null) {
                attributes.put("user", new StompPrincipal(idUsuario)); // Guardamos el principal en atributos
                return true;
            }
        }
        // 3. Si no hay token o no es válido, rechazar la conexión
        log.warn("Rechazando handshake de WebSocket: Token no válido o ausente.");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {}

    private String getTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("jwt".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String getToken(HttpServletRequest request) {
        String query = request.getQueryString();
        if (query == null || !query.contains("token=")) {
            return null; // No hay parámetro "token"
        }

        String token = null;
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                token = param.substring(6); // Quita "token="
                break;
            }
        }

        if (token == null) {
            return null;
        }

        try {
            // Validar y decodificar el token usando tu JWTUtils
            Jws<Claims> payload = jwtUtil.decodificarJwt(token);
            // Extraer el "subject", que debe ser tu ID de usuario
            return payload.getPayload().getSubject();
        } catch (Exception e) {
            // El token es inválido, expiró, etc.
            log.error("Error al validar token de WebSocket: {}", e.getMessage());
            return null;
        }
    }
}
