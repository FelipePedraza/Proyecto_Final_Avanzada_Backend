package co.edu.uniquindio.application.dtos.alojamiento;

import co.edu.uniquindio.application.models.enums.Servicio;
import jakarta.validation.constraints.*;

import java.util.List;

public record AlojamientoDTO(
        @NotBlank
        String titulo,

        @NotBlank
        String descripcion,

        @NotNull
        DireccionDTO direccion,

        @NotNull @Min(0)
        Float precioPorNoche,

        @NotNull @Min(1)
        Integer maxHuespedes,

        List<Servicio> servicios,

        @NotNull @Size(min = 1, max = 10)
        List<String> imagenes,

        @Min(0)
        Integer imagenPrincipal
) {
}