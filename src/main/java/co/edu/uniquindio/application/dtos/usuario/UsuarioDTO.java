package co.edu.uniquindio.application.dtos.usuario;

import co.edu.uniquindio.application.models.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

public record UsuarioDTO(
        @NotBlank Long id,
        @NotBlank @Length(max = 100) String nombre,
        @NotBlank @Length(max = 50) @Email String email,
        @Length(max = 20) String telefono,
        @URL String foto,
        @NotNull @Past LocalDate fechaNacimiento,
        @NotNull Rol rol
) {

}