package co.edu.uniquindio.application.dtos.usuario;

import co.edu.uniquindio.application.models.enums.Rol;

import java.time.LocalDate;

public record UsuarioDTO(
        String id,
        String nombre,
        String email,
        String telefono,
        Rol rol,
        LocalDate fechaNacimiento,
        String foto,
        Boolean esAnfitrion
) {
}
