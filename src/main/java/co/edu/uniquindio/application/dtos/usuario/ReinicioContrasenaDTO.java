package co.edu.uniquindio.application.dtos.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record ReinicioContrasenaDTO(
        @NotBlank @Email
        String email,
        @NotBlank
        String codigoVerificacion,
        @NotBlank @Length(min = 8) @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$", message = "La contraseña debe tener al menos 8 caracteres, una mayúscula y un número")
        String nuevaContrasena
) {
}