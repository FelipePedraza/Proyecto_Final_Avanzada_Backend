package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.chat.ChatDTO;
import co.edu.uniquindio.application.dtos.chat.MensajeDTO;
import co.edu.uniquindio.application.services.ChatServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatControlador {

    private final ChatServicio chatServicio;

    @GetMapping("/{chatId}")
    public ResponseEntity<RespuestaDTO<ChatDTO>> obtenerChat(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) throws Exception {
        ChatDTO chat = chatServicio.obtenerChat(chatId, pagina, tamano);
        return ResponseEntity.ok(new RespuestaDTO<>(false, chat));
    }

    @PostMapping("/enviar")
    public ResponseEntity<RespuestaDTO<MensajeDTO>> enviarMensaje(
            @RequestParam String remitenteId,
            @RequestParam String destinatarioId,
            @Valid @RequestBody String contenido) throws Exception {
        MensajeDTO mensaje = chatServicio.enviarMensaje(remitenteId, destinatarioId, contenido);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RespuestaDTO<>(false, mensaje));
    }

    @GetMapping("/usuario/{id}/conversaciones")
    public ResponseEntity<RespuestaDTO<List<ChatDTO>>> listarConversaciones(@PathVariable String id) throws Exception {
        List<ChatDTO> conversaciones = chatServicio.listarConversaciones(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, conversaciones));
    }

    @GetMapping("/usuario/{id}/mensajes-no-leidos")
    public ResponseEntity<RespuestaDTO<Long>> obtenerMensajesNoLeidos(@PathVariable String id) throws Exception {
        Long cantidad = chatServicio.obtenerMensajesNoLeidos(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, cantidad));
    }

    @PutMapping("/{chatId}/marcar-leido")
    public ResponseEntity<RespuestaDTO<String>> marcarChatComoLeido(
            @PathVariable Long chatId,
            @RequestParam String usuarioId) throws Exception {
        chatServicio.marcarChatComoLeido(chatId, usuarioId);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Chat marcado como leído"));
    }

    @PostMapping("/iniciar")
    public ResponseEntity<RespuestaDTO<ChatDTO>> iniciarChatConUsuario(
            @RequestParam String destinatarioId) throws Exception {
        ChatDTO chat = chatServicio.iniciarChatConUsuario(destinatarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RespuestaDTO<>(false, chat));
    }
}
