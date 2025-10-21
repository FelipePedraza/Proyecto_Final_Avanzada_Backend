package co.edu.uniquindio.application.config.ws;

import co.edu.uniquindio.application.security.JWTUtils;
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
                try {
                    attributes.put("user", new StompPrincipal(idUsuario)); // Guardamos el principal en atributos
                } catch (Exception e) {
                    // Token inválido, rechazar la conexión
                    return false;
                }
            }
        }
        return true;
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
        if (query != null && query.contains("id=")) {
            for (String param : query.split("&")) {
                if (param.startsWith("id=")) {
                    return param.substring(3); // Quita "token="
                }
            }
        }
        return null;
    }
}
