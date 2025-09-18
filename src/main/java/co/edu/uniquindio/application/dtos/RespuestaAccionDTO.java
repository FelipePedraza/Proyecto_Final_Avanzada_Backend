package co.edu.uniquindio.application.dtos;

import co.edu.uniquindio.application.models.enums.Accion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RespuestaAccionDTO(
        @NotNull
        Accion accion,
        @NotBlank
        String mensaje
) {
}
