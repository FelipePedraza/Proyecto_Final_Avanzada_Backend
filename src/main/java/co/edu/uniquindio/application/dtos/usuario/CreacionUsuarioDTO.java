package co.edu.uniquindio.application.dtos.usuario;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

public record CreacionUsuarioDTO(

        @NotBlank @Length(max = 100)
        String nombre,

        @NotBlank @Length(max = 50)
        @Email
        String email,

        @NotBlank @Length(min = 8)
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$", message = "La contraseña debe tener al menos 8 caracteres, una mayúscula y un número")
        String contrasena,

        @Length(max = 10)
        String telefono,

        @Length(max = 300)
        String foto,

        @NotNull @Past
        LocalDate fechaNacimiento
) {
}