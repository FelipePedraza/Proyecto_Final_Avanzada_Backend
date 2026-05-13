package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.chat.ChatDTO;
import co.edu.uniquindio.application.dtos.chat.MensajeDTO;
import co.edu.uniquindio.application.exceptions.NoFoundException;
import co.edu.uniquindio.application.exceptions.ValidationException;
import co.edu.uniquindio.application.mappers.ChatMapper;
import co.edu.uniquindio.application.mappers.MensajeMapper;
import co.edu.uniquindio.application.models.entitys.Chat;
import co.edu.uniquindio.application.models.entitys.Mensaje;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.models.enums.Estado;
import co.edu.uniquindio.application.repositories.ChatRepositorio;
import co.edu.uniquindio.application.repositories.MensajeRepositorio;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.services.AuthServicio;
import co.edu.uniquindio.application.services.ChatServicio;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServicioImpl implements ChatServicio {

    private final ChatRepositorio chatRepositorio;
    private final MensajeRepositorio mensajeRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final ChatMapper chatMapper;
    private final MensajeMapper mensajeMapper;
    private final AuthServicio authServicio;
    private final MeterRegistry meterRegistry;

    @Override
    public ChatDTO obtenerChat(Long chatId, int pagina, int tamano) throws Exception {
        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();

        Chat chat = chatRepositorio.findById(chatId)
                .orElseThrow(() -> new NoFoundException("Chat no encontrado"));

        if (!chatRepositorio.esParticipanteDelChat(chatId, idUsuarioAutenticado)) {
            throw new AccessDeniedException("No tienes permisos para acceder a este chat");
        }

        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by("fechaEnvio").ascending());
        Page<Mensaje> mensajesPage = mensajeRepositorio.buscarPorChatId(chatId, pageable);
        
        chat.setMensajes(mensajesPage.getContent());
        mensajeRepositorio.marcarMensajesComoLeidos(chatId, idUsuarioAutenticado);

        return chatMapper.toDTO(chat);
    }

    @Override
    public MensajeDTO enviarMensaje(String remitenteId, String destinatarioId, String contenido) throws Exception {
        if (contenido == null || contenido.trim().isEmpty()) {
            throw new ValidationException("El contenido del mensaje no puede estar vacío");
        }

        if (contenido.length() > 1000) {
            throw new ValidationException("El mensaje no puede exceder los 1000 caracteres");
        }

        if (remitenteId.equals(destinatarioId)) {
            throw new ValidationException("No puedes enviarte mensajes a ti mismo");
        }

        Usuario destinatario = usuarioRepositorio.findById(destinatarioId)
                .orElseThrow(() -> new NoFoundException("Usuario destinatario no encontrado"));

        if (destinatario.getEstado() == Estado.ELIMINADO) {
            throw new ValidationException("El usuario destinatario no está disponible");
        }

        Usuario remitente = usuarioRepositorio.findById(remitenteId)
                .orElseThrow(() -> new NoFoundException("Usuario remitente no encontrado"));

        Chat chat = buscarOCrearChatEntreUsuarios(remitenteId, destinatarioId);

        Mensaje mensaje = Mensaje.builder()
                .contenido(contenido.trim())
                .remitente(remitente)
                .destinatario(destinatario)
                .chat(chat)
                .build();

        mensaje = mensajeRepositorio.save(mensaje);
        meterRegistry.counter("chat.mensajes.enviados").increment();

        return mensajeMapper.toDTO(mensaje);
    }

    @Override
    public List<ChatDTO> listarConversaciones(String usuarioId) throws Exception {
        if (!authServicio.obtnerIdAutenticado(usuarioId)) {
            throw new AccessDeniedException("No tienes permisos para ver las conversaciones de este usuario");
        }

        List<Chat> chats = chatRepositorio.findChatsByUsuario(usuarioId);

        return chats.stream()
                .map(chatMapper::toDTO)
                .toList();
    }

    @Override
    public ChatDTO iniciarChatConUsuario(String remitenteId, String destinatarioId) throws Exception {

        if (remitenteId.equals(destinatarioId)) {
            throw new ValidationException("No puedes iniciar un chat contigo mismo");
        }

        Usuario destinatario = usuarioRepositorio.findById(destinatarioId)
                .orElseThrow(() -> new NoFoundException("Usuario destinatario no encontrado"));

        if (destinatario.getEstado() == Estado.ELIMINADO) {
            throw new ValidationException("El usuario destinatario no está disponible");
        }

        Chat chat = buscarOCrearChatEntreUsuarios(remitenteId, destinatarioId);

        return chatMapper.toDTO(chat);
    }

    private Chat buscarOCrearChatEntreUsuarios(String usuario1Id, String usuario2Id) {
        Optional<Chat> chatExistente = chatRepositorio.findChatEntreUsuarios(usuario1Id, usuario2Id);
        
        if (chatExistente.isPresent()) {
            return chatExistente.get();
        }

        Usuario usuario1 = usuarioRepositorio.findById(usuario1Id)
                .orElseThrow(() -> new NoFoundException("Usuario 1 no encontrado"));
        Usuario usuario2 = usuarioRepositorio.findById(usuario2Id)
                .orElseThrow(() -> new NoFoundException("Usuario 2 no encontrado"));

        Chat nuevoChat = Chat.builder()
                .usuario1(usuario1)
                .usuario2(usuario2)
                .activo(true)
                .build();

        return chatRepositorio.save(nuevoChat);
    }

    @Override
    public Long obtenerMensajesNoLeidos(String usuarioId) throws Exception {
        if (!authServicio.obtnerIdAutenticado(usuarioId)) {
            throw new AccessDeniedException("No tienes permisos para ver los mensajes de este usuario");
        }

        List<Mensaje> mensajesNoLeidos = mensajeRepositorio.findMensajesNoLeidosPorUsuario(usuarioId);
        return (long) mensajesNoLeidos.size();
    }

    @Override
    public void marcarChatComoLeido(Long chatId, String usuarioId) throws Exception {
        if (!authServicio.obtnerIdAutenticado(usuarioId)) {
            throw new AccessDeniedException("No tienes permisos para marcar mensajes de este usuario");
        }

        if (!chatRepositorio.esParticipanteDelChat(chatId, usuarioId)) {
            throw new AccessDeniedException("No tienes permisos para acceder a este chat");
        }

        mensajeRepositorio.marcarMensajesComoLeidos(chatId, usuarioId);
    }
}
