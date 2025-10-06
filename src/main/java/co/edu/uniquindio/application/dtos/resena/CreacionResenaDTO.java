package co.edu.uniquindio.application.dtos.resena;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record CreacionResenaDTO(
        @NotNull @Min(1) @Max(5)
        Float calificacion,
        @NotBlank @Length(max = 500)
        String comentario,
        @NotNull
        Long reservaId
) {
}