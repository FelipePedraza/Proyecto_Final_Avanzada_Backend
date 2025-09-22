package co.edu.uniquindio.application.dtos.usuario;

import org.hibernate.validator.constraints.Length;

import co.edu.uniquindio.application.models.enums.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record EdicionUsuarioDTO(
        @NotBlank @Length(max = 100) String nombre,
        @Length(max = 20) String telefono,
        @URL String foto,
        @NotNull Rol rol
) {
}