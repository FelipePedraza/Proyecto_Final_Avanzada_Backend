package co.edu.uniquindio.application.dtos.alojamiento;

import co.edu.uniquindio.application.models.enums.Ciudad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DireccionDTO(
        @NotBlank
        Ciudad ciudad,
        @NotBlank
        String direccion,
        @NotNull
        LocalizacionDTO localizacion
) {
}