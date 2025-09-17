package co.edu.uniquindio.application.dtos;

import java.time.LocalDateTime;

public record ErrorDTO(
        String error,
        String mensaje,
        LocalDateTime timestamp,
        String path
) {
}
