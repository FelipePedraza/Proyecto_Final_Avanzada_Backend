package co.edu.uniquindio.application.dtos.chat;

import co.edu.uniquindio.application.dtos.usuario.UsuarioDTO;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ChatDTO(
        @NotNull
        Integer reservaId,
        List<UsuarioDTO> participantes,
        List<MensajeDTO> mensajes,
        MensajeDTO ultimoMensaje
) {
}
