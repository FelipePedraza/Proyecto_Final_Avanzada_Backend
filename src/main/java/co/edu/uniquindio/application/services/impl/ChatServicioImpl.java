package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.chat.ChatDTO;
import co.edu.uniquindio.application.dtos.chat.MensajeDTO;
import co.edu.uniquindio.application.services.ChatServicio;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServicioImpl implements ChatServicio {

    @Override
    public ChatDTO obtenerChat(Long id, int pagina, int tamano) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public MensajeDTO enviarMensaje(Long id, String contenido) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public List<ChatDTO> listarConversaciones(Long id) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public void marcarMensajeLeido(Long id) throws Exception {
        // Lógica de negocio a implementar
    }
}
