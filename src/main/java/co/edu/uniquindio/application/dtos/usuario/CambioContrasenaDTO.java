package co.edu.uniquindio.application.dtos.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

/**
 * DTO para cambio de contraseña.
 * Validación: mínimo 8 caracteres, al menos una mayúscula y un dígito.
 */
public record CambioContrasenaDTO(
        @NotBlank @Length(min = 8)
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$",
                 message = "La contraseña debe tener al menos 8 caracteres, una mayúscula y un número")
        String newPassword
) {}