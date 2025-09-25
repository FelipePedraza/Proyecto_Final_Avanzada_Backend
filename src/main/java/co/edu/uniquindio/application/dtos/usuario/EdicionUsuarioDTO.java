package co.edu.uniquindio.application.dtos.usuario;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import java.time.LocalDate;

public record EdicionUsuarioDTO(
        @NotBlank @Length(max = 100) String nombre,
        @Length(max = 10) String telefono,
        @Length(max = 300) String foto,
        @NotNull LocalDate fechaNacimiento
        ) {
}