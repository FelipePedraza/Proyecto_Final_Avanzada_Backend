package co.edu.uniquindio.application.dtos.chat;

import co.edu.uniquindio.application.dtos.usuario.UsuarioDTO;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record ChatDTO(
        @NotNull
        Long id,
        UsuarioDTO usuario1,
        UsuarioDTO usuario2,
        List<MensajeDTO> mensajes,
        MensajeDTO ultimoMensaje,
        LocalDateTime creadoEn,
        boolean activo
) {
}
