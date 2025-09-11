package co.edu.uniquindio.application.dtos.Usuarios;

import java.time.LocalDate;

import co.edu.uniquindio.application.models.entitys.Ubicacion;
import co.edu.uniquindio.application.models.enums.Rol;


public record CreacionUsuarioDTO(    
    String nombre,
    String email,
    String passwordHash,
    String telefono,
    LocalDate fechaNacimiento,
    String fotoUrl,
    Ubicacion ubicacion,
    Rol rol
    ) {

}
