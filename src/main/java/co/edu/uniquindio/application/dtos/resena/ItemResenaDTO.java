package co.edu.uniquindio.application.dtos.resena;

import co.edu.uniquindio.application.dtos.usuario.UsuarioDTO;

import java.time.LocalDateTime;

public record ItemResenaDTO(
        Long id,
        Float calificacion,
        String comentario,
        LocalDateTime creadoEn,
        UsuarioDTO usuario,
        String respuesta
) {
}