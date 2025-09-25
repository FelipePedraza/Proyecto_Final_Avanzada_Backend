package co.edu.uniquindio.application.dtos.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record CambioContrasenaDTO(

        @NotBlank
        String contrasenaActual,

        @NotBlank @Length(min = 8) @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$", message = "La contraseña debe tener al menos 8 caracteres, una mayúscula y un número")
        String contrasenaNueva
) {
}

