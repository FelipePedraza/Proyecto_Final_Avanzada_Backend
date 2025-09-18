package co.edu.uniquindio.application.dtos.resena;

import jakarta.validation.constraints.NotBlank;

public record CreacionRespuestaDTO(
        @NotBlank
        String mensaje
) {
}