package co.edu.uniquindio.application.dtos.usuario;

import jakarta.validation.constraints.Past;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

public record EdicionUsuarioDTO(
        @Length(max = 100) String nombre,
        @Length(max = 10) String telefono,
        @Length(max = 300) String foto,
        @Past LocalDate fechaNacimiento
        ) {
}