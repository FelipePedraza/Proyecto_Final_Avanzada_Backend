package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.chat.ChatDTO;
import co.edu.uniquindio.application.dtos.chat.MensajeDTO;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public interface ChatServicio {
    ChatDTO obtenerChat(Long chatId, int pagina, int tamano) throws Exception;
    MensajeDTO enviarMensaje(String remitenteId, String destinatarioId, String contenido) throws Exception;
    List<ChatDTO> listarConversaciones(String usuarioId) throws Exception;
    Long obtenerMensajesNoLeidos(String usuarioId) throws Exception;
    void marcarChatComoLeido(Long chatId, String usuarioId) throws Exception;
    ChatDTO iniciarChatConUsuario(String destinatarioId) throws Exception;
}
