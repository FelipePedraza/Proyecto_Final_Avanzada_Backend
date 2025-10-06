package co.edu.uniquindio.application.dtos.alojamiento;

import co.edu.uniquindio.application.models.enums.Servicio;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record EdicionAlojamientoDTO(

        @NotBlank @Length(max = 150)
        String titulo,

        @NotBlank
        String descripcion,

        @NotNull 
        int maxHuespedes,

        @NotNull @Min(0)
        Float precioPorNoche,

        @NotNull
        List<Servicio> servicios,

        @NotNull @Size(max = 10)
        List<String> imagenes,

        @NotNull
        DireccionDTO direccion
) {
}