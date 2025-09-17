package co.edu.uniquindio.application.dtos.usuario;

import co.edu.uniquindio.application.models.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.List;

public record CreacionAnfitrionDTO(
        @NotBlank
        String nombre,
        @NotBlank @Email
        String email,
        @NotBlank @Length(min = 8) @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$", message = "La contraseña debe tener al menos 8 caracteres, una mayúscula y un número")
        String contrasena,
        @NotBlank
        String telefono,
        @NotNull
        Rol rol,
        @NotNull
        LocalDate fechaNacimiento,
        String foto,
        String descripcion,
        List<String> documentos
) {
}