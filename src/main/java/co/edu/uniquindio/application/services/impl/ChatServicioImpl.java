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
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Override
    public ChatDTO obtenerChat(Long chatId, int pagina, int tamano) throws Exception {
        // Obtener usuario autenticado
        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();

        // Obtener y validar chat
        Chat chat = chatRepositorio.findById(chatId)
                .orElseThrow(() -> new NoFoundException("Chat no encontrado"));

        // Verificar que el usuario sea participante del chat
        if (!chatRepositorio.esParticipanteDelChat(chatId, idUsuarioAutenticado)) {
            throw new AccessDeniedException("No tienes permisos para acceder a este chat");
        }

        // Obtener mensajes con paginación
        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Mensaje> mensajesPage = mensajeRepositorio.findByChat_IdOrderByFechaEnvioAsc(chatId, pageable);
        
        // Cargar mensajes en el chat para el mapper
        chat.setMensajes(mensajesPage.getContent());

        // Marcar mensajes como leídos para el usuario actual
        mensajeRepositorio.marcarMensajesComoLeidos(chatId, idUsuarioAutenticado);

        return chatMapper.toDTO(chat);
    }

    @Override
    // 1. Añadir 'String remitenteId' a la firma del método
    public MensajeDTO enviarMensaje(String remitenteId, String destinatarioId, String contenido) throws Exception {
        // Validar contenido
        if (contenido == null || contenido.trim().isEmpty()) {
            throw new ValidationException("El contenido del mensaje no puede estar vacío");
        }

        if (contenido.length() > 1000) {
            throw new ValidationException("El mensaje no puede exceder los 1000 caracteres");
        }

        // Validar que no se envíe mensaje a sí mismo
        if (remitenteId.equals(destinatarioId)) {
            throw new ValidationException("No puedes enviarte mensajes a ti mismo");
        }

        // Obtener y validar destinatario
        Usuario destinatario = usuarioRepositorio.findById(destinatarioId)
                .orElseThrow(() -> new NoFoundException("Usuario destinatario no encontrado"));

        if (destinatario.getEstado() == Estado.ELIMINADO) {
            throw new ValidationException("El usuario destinatario no está disponible");
        }

        // Obtener remitente
        Usuario remitente = usuarioRepositorio.findById(remitenteId)
                .orElseThrow(() -> new NoFoundException("Usuario remitente no encontrado"));

        // Buscar o crear chat entre los usuarios
        Chat chat = buscarOCrearChatEntreUsuarios(remitenteId, destinatarioId);

        // Crear mensaje
        Mensaje mensaje = Mensaje.builder()
                .contenido(contenido.trim())
                .remitente(remitente)
                .destinatario(destinatario)
                .chat(chat)
                .build();

        mensaje = mensajeRepositorio.save(mensaje);

        return mensajeMapper.toDTO(mensaje);
    }

    @Override
    public List<ChatDTO> listarConversaciones(String usuarioId) throws Exception {
        // Verificar permisos
        if (!authServicio.obtnerIdAutenticado(usuarioId)) {
            throw new AccessDeniedException("No tienes permisos para ver las conversaciones de este usuario");
        }

        // Obtener chats del usuario
        List<Chat> chats = chatRepositorio.findChatsByUsuario(usuarioId);

        // Convertir a DTOs
        return chats.stream()
                .map(chatMapper::toDTO)
                .toList();
    }

    @Override
    public ChatDTO iniciarChatConUsuario(String destinatarioId) throws Exception {
        // Obtener usuario autenticado
        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();

        // Validar que no se inicie chat consigo mismo
        if (idUsuarioAutenticado.equals(destinatarioId)) {
            throw new ValidationException("No puedes iniciar un chat contigo mismo");
        }

        // Validar destinatario
        Usuario destinatario = usuarioRepositorio.findById(destinatarioId)
                .orElseThrow(() -> new NoFoundException("Usuario destinatario no encontrado"));

        if (destinatario.getEstado() == Estado.ELIMINADO) {
            throw new ValidationException("El usuario destinatario no está disponible");
        }

        // Buscar o crear chat
        Chat chat = buscarOCrearChatEntreUsuarios(idUsuarioAutenticado, destinatarioId);

        return chatMapper.toDTO(chat);
    }

    /**
     * Busca un chat existente o crea uno nuevo entre dos usuarios
     */
    private Chat buscarOCrearChatEntreUsuarios(String usuario1Id, String usuario2Id) {
        Optional<Chat> chatExistente = chatRepositorio.findChatEntreUsuarios(usuario1Id, usuario2Id);
        
        if (chatExistente.isPresent()) {
            return chatExistente.get();
        }

        // Obtener usuarios
        Usuario usuario1 = usuarioRepositorio.findById(usuario1Id)
                .orElseThrow(() -> new NoFoundException("Usuario 1 no encontrado"));
        Usuario usuario2 = usuarioRepositorio.findById(usuario2Id)
                .orElseThrow(() -> new NoFoundException("Usuario 2 no encontrado"));

        // Crear nuevo chat
        Chat nuevoChat = Chat.builder()
                .usuario1(usuario1)
                .usuario2(usuario2)
                .activo(true)
                .build();

        return chatRepositorio.save(nuevoChat);
    }

    /**
     * Obtiene el número de mensajes no leídos para un usuario
     */
    @Override
    public Long obtenerMensajesNoLeidos(String usuarioId) throws Exception {
        if (!authServicio.obtnerIdAutenticado(usuarioId)) {
            throw new AccessDeniedException("No tienes permisos para ver los mensajes de este usuario");
        }

        List<Mensaje> mensajesNoLeidos = mensajeRepositorio.findMensajesNoLeidosPorUsuario(usuarioId);
        return (long) mensajesNoLeidos.size();
    }

    /**
     * Marca todos los mensajes de un chat como leídos para un usuario
     */
    @Override
    public void marcarChatComoLeido(Long chatId, String usuarioId) throws Exception {
        if (!authServicio.obtnerIdAutenticado(usuarioId)) {
            throw new AccessDeniedException("No tienes permisos para marcar mensajes de este usuario");
        }

        // Verificar que el usuario sea participante del chat
        if (!chatRepositorio.esParticipanteDelChat(chatId, usuarioId)) {
            throw new AccessDeniedException("No tienes permisos para acceder a este chat");
        }

        mensajeRepositorio.marcarMensajesComoLeidos(chatId, usuarioId);
    }
}
