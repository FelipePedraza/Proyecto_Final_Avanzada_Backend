package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.chat.ChatDTO;
import co.edu.uniquindio.application.dtos.chat.MensajeDTO;

import java.util.List;

public interface ChatServicio {
    ChatDTO obtenerChat(Long id, int pagina, int tamano) throws Exception;
    MensajeDTO enviarMensaje(Long id, String contenido) throws Exception;
    List<ChatDTO> listarConversaciones(Long id) throws Exception;
}
