package co.edu.uniquindio.application.dtos.alojamiento;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LocalizacionDTO(
        @NotNull @Min(-90) @Max(90)
        Double latitud,
        @NotNull @Min(-180) @Max(180)
        Double longitud
) {
}