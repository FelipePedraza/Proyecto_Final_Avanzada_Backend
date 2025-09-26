package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.chat.ChatDTO;
import co.edu.uniquindio.application.dtos.chat.MensajeDTO;
import co.edu.uniquindio.application.mappers.ChatMapper;
import co.edu.uniquindio.application.models.entitys.Chat;
import co.edu.uniquindio.application.models.entitys.Mensaje;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.repositories.ChatRepositorio;
import co.edu.uniquindio.application.repositories.MensajeRepositorio;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServicioImpl implements co.edu.uniquindio.application.services.ChatServicio {

    private final ChatRepositorio chatRepositorio;
    private final MensajeRepositorio mensajeRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final ChatMapper chatMapper;

    @Override
    public ChatDTO obtenerChat(Long id, int pagina, int tamano) throws Exception {
        Chat chat = chatRepositorio.findById(id)
                .orElseThrow(() -> new Exception("Chat no encontrado"));
        List<Mensaje> mensajes = mensajeRepositorio.findByChatIdOrderByFechaEnvioAsc(id);
        int fromIndex = Math.min(pagina * tamano, mensajes.size());
        int toIndex = Math.min(fromIndex + tamano, mensajes.size());
        List<MensajeDTO> mensajesPaginados = mensajes.subList(fromIndex, toIndex)
                .stream().map(chatMapper::toMensajeDTO).toList();
        List<co.edu.uniquindio.application.dtos.usuario.UsuarioDTO> participantesDTO = chat.getParticipantes().stream()
                .map(chatMapper::toUsuarioDTO).toList();
        return new ChatDTO(
                chat.getReserva().getId().intValue(),
                participantesDTO,
                mensajesPaginados,
                mensajesPaginados.isEmpty() ? null : mensajesPaginados.get(mensajesPaginados.size() - 1)
        );
    }

    @Override
    public MensajeDTO enviarMensaje(Long id, String contenido) throws Exception {
        Chat chat = chatRepositorio.findById(id)
                .orElseThrow(() -> new Exception("Chat no encontrado"));
        Usuario remitente = chat.getParticipantes().get(0);
        Usuario destinatario = chat.getParticipantes().size() > 1 ? chat.getParticipantes().get(1) : remitente;
        Mensaje mensaje = Mensaje.builder()
                .chat(chat)
                .remitente(remitente)
                .destinatario(destinatario)
                .contenido(contenido)
                .fechaEnvio(LocalDateTime.now())
                .leido(false)
                .build();
        mensajeRepositorio.save(mensaje);
        return chatMapper.toMensajeDTO(mensaje);
    }

    @Override
    public List<ChatDTO> listarConversaciones(Long id) throws Exception {
        Usuario usuario = usuarioRepositorio.findById(id.toString())
                .orElseThrow(() -> new Exception("Usuario no encontrado"));
        List<Chat> chats = chatRepositorio.findByParticipantes_Id(usuario.getId());
        return chats.stream().map(chatMapper::toDTO).toList();
    }

    @Override
    public void marcarMensajeLeido(Long id) throws Exception {
        Mensaje mensaje = mensajeRepositorio.findById(id)
                .orElseThrow(() -> new Exception("Mensaje no encontrado"));
        mensaje.setLeido(true);
        mensajeRepositorio.save(mensaje);
    }
}