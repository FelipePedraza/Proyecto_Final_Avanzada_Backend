package co.edu.uniquindio.application.dtos.alojamiento;

import co.edu.uniquindio.application.models.enums.Servicio;
import jakarta.validation.constraints.*;

import java.util.List;

public record EdicionAlojamientoDTO(

        @NotBlank
        String titulo,

        @NotBlank
        String descripcion,

        @NotNull @Min(0)
        Float precioNoche,

        @NotNull @Min(1)
        Integer capacidad,

        List<Servicio> servicios,

        @NotNull @Size(min = 1, max = 10)
        List<String> imagenes,

        @Min(0)
        Integer imagenPrincipal,

        DireccionDTO ubicacion
) {
}