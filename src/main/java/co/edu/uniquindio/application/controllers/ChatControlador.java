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

    @GetMapping("/reserva/{id}")
    public ResponseEntity<RespuestaDTO<ChatDTO>> obtenerChat(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) throws Exception {
        ChatDTO chat = chatServicio.obtenerChat(id, pagina, tamano);
        return ResponseEntity.ok(new RespuestaDTO<>(false, chat));
    }

    @PostMapping("/reserva/{id}/mensajes")
    public ResponseEntity<RespuestaDTO<MensajeDTO>> enviarMensaje(
            @PathVariable Long id,
            @Valid @RequestBody String contenido) throws Exception {
        MensajeDTO mensaje = chatServicio.enviarMensaje(id, contenido);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RespuestaDTO<>(false, mensaje));
    }

    @GetMapping("/usuario/{id}/conversaciones")
    public ResponseEntity<RespuestaDTO<List<ChatDTO>>> listarConversaciones(@PathVariable Long id) throws Exception {
        List<ChatDTO> conversaciones = chatServicio.listarConversaciones(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, conversaciones));
    }

    @PatchMapping("/mensajes/{id}/estado")
    public ResponseEntity<RespuestaDTO<String>> marcarMensajeLeido(@PathVariable Long id) throws Exception {
        chatServicio.marcarMensajeLeido(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Mensaje marcado como leído."));
    }
}
