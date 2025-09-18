package co.edu.uniquindio.application.dtos.resena;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record ComentarioDTO(
        @NotNull @Min(1) @Max(5)
        Integer calificacion,
        @NotBlank @Length(min = 1, max = 500)
        String comentario,
        @NotNull
        Integer reservaId,
        @NotNull
        Integer usuarioId,
        @NotNull
        Integer alojamientoId
) {
}
