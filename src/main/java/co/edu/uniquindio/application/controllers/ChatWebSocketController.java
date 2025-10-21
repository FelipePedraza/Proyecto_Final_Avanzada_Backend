package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.chat.MensajeDTO;
import co.edu.uniquindio.application.services.ChatServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.security.Principal; // <-- Importar

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatServicio chatServicio;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    // 1. Añadir Principal
    public void sendMessage(@Payload MensajeDTO mensaje, Principal principal) {

        // 2. Obtener el remitente VERDADERO desde el Principal
        String remitenteId = principal.getName();

        try {
            // 3. Pasar el remitenteId autenticado al servicio
            MensajeDTO mensajeEnviado = chatServicio.enviarMensaje(
                    remitenteId, // <-- Usar el ID del Principal
                    mensaje.destinatarioId(),
                    mensaje.contenido()
            );

            // Enviar al destinatario específico
            messagingTemplate.convertAndSendToUser(
                    mensaje.destinatarioId(),
                    "/queue/private", // <-- CORRECCIÓN: Usar /queue para coincidir con el cliente
                    mensajeEnviado
            );

            // Enviar confirmación al remitente
            messagingTemplate.convertAndSendToUser(
                    remitenteId, // <-- Usar el ID del Principal
                    "/queue/private", // <-- CORRECCIÓN: Usar /queue
                    mensajeEnviado
            );

        } catch (Exception e) {
            // Enviar error al remitente
            messagingTemplate.convertAndSendToUser(
                    remitenteId, // <-- Usar el ID del Principal
                    "/queue/errors", // <-- CORRECCIÓN: Usar /queue
                    "Error al enviar mensaje: " + e.getMessage()
            );
        }
    }

    @MessageMapping("/chat.join")
    // 4. Cambiar @Payload por Principal
    public void joinChat(Principal principal) {
        String usuarioId = principal.getName(); // Obtener ID del Principal
        try {
            // Notificar que el usuario se ha conectado
            messagingTemplate.convertAndSendToUser(
                    usuarioId,
                    "/queue/status", // <-- CORRECCIÓN: Usar /queue
                    "Conectado al chat"
            );
        } catch (Exception e) {
            // Manejar errores de conexión
            messagingTemplate.convertAndSendToUser(
                    usuarioId,
                    "/queue/errors", // <-- CORRECCIÓN: Usar /queue
                    "Error al unirse al chat: " + e.getMessage()
            );
        }
    }

    @MessageMapping("/chat.leave")
    // 5. Cambiar @Payload por Principal
    public void leaveChat(Principal principal) {
        String usuarioId = principal.getName(); // Obtener ID del Principal
        try {
            // Notificar que el usuario se ha desconectado
            messagingTemplate.convertAndSendToUser(
                    usuarioId,
                    "/queue/status", // <-- CORRECCIÓN: Usar /queue
                    "Desconectado del chat"
            );
        } catch (Exception e) {
            // Manejar errores de desconexión
            messagingTemplate.convertAndSendToUser(
                    usuarioId,
                    "/queue/errors", // <-- CORRECCIÓN: Usar /queue
                    "Error al desconectarse del chat: " + e.getMessage()
            );
        }
    }
}