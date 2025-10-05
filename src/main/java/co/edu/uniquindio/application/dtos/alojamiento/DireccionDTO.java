package co.edu.uniquindio.application.dtos.alojamiento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DireccionDTO(
        @NotBlank
        String ciudad,
        @NotBlank
        String direccion,
        @NotNull
        LocalizacionDTO localizacion
) {
}