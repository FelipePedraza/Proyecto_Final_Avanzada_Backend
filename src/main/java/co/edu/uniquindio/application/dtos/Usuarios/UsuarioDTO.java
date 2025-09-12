package co.edu.uniquindio.application.dtos.Usuarios;

import co.edu.uniquindio.application.models.enums.Rol;
import java.time.LocalDate;

public record UsuarioDTO(
    Long id,
    String nombre,
    String email,
    String telefono,
    String foto,
    LocalDate fechaNacimiento,
    Rol rol
) {

}