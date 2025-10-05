package co.edu.uniquindio.application.dtos.alojamiento;

import co.edu.uniquindio.application.models.enums.Servicio;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record CreacionAlojamientoDTO(

        @NotBlank @Length(max = 150) 
        String titulo,

        @NotBlank
        String descripcion,

        @NotNull 
        int maxHuespedes,

        @NotNull
        DireccionDTO direccion,

        @NotNull @Min(0)
        Float precioPorNoche,

        List<Servicio> servicios,

        @Size(min = 1, max = 10)
        List<String> imagenes

) {
}
