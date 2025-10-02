package co.edu.uniquindio.application.dtos.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank @Email
        String email,
        @NotBlank
        String contrasena
) {
}