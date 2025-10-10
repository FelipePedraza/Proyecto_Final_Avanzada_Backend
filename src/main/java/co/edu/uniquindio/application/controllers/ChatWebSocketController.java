package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.chat.MensajeDTO;
import co.edu.uniquindio.application.services.ChatServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatServicio chatServicio;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MensajeDTO mensaje) {
        try {
            // Enviar mensaje usando el servicio
            MensajeDTO mensajeEnviado = chatServicio.enviarMensaje(
                mensaje.destinatarioId(), 
                mensaje.contenido()
            );
            
            // Enviar al destinatario específico
            messagingTemplate.convertAndSendToUser(
                mensaje.destinatarioId(), 
                "/queue/private", 
                mensajeEnviado
            );
            
            // Enviar confirmación al remitente
            messagingTemplate.convertAndSendToUser(
                mensaje.remitenteId(), 
                "/queue/private", 
                mensajeEnviado
            );
            
        } catch (Exception e) {
            // Enviar error al remitente
            messagingTemplate.convertAndSendToUser(
                mensaje.remitenteId(), 
                "/queue/errors", 
                "Error al enviar mensaje: " + e.getMessage()
            );
        }
    }

    @MessageMapping("/chat.join")
    public void joinChat(@Payload String usuarioId) {
        try {
            // Notificar que el usuario se ha conectado
            messagingTemplate.convertAndSendToUser(
                usuarioId, 
                "/queue/status", 
                "Conectado al chat"
            );
        } catch (Exception e) {
            // Manejar errores de conexión
            messagingTemplate.convertAndSendToUser(
                usuarioId, 
                "/queue/errors", 
                "Error al unirse al chat: " + e.getMessage()
            );
        }
    }

    @MessageMapping("/chat.leave")
    public void leaveChat(@Payload String usuarioId) {
        try {
            // Notificar que el usuario se ha desconectado
            messagingTemplate.convertAndSendToUser(
                usuarioId, 
                "/queue/status", 
                "Desconectado del chat"
            );
        } catch (Exception e) {
            // Manejar errores de desconexión
            messagingTemplate.convertAndSendToUser(
                usuarioId, 
                "/queue/errors", 
                "Error al desconectarse del chat: " + e.getMessage()
            );
        }
    }
}
