package co.edu.uniquindio.application.dtos.resena;

import co.edu.uniquindio.application.dtos.usuario.UsuarioDTO;
import co.edu.uniquindio.application.models.vo.Respuesta;

import java.time.LocalDateTime;

public record ItemResenaDTO(
        Long id,
        Float calificacion,
        String comentario,
        LocalDateTime creadoEn,
        UsuarioDTO usuario,
        Respuesta respuesta
) {
}