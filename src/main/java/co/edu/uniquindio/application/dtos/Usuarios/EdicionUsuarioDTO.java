package co.edu.uniquindio.application.dtos.Usuarios;

import org.hibernate.validator.constraints.Length;

import co.edu.uniquindio.application.models.enums.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EdicionUsuarioDTO(
    Long id,
    @NotBlank @Length(max = 100) String nombre,
    @Length(max = 20) String telefono,
    String foto,
    @NotNull Rol rol
) { 

}
