package co.edu.uniquindio.application.mappers;

import co.edu.uniquindio.application.dtos.Usuarios.UsuarioDTO;
import co.edu.uniquindio.application.dtos.Usuarios.CreacionUsuarioDTO;
import co.edu.uniquindio.application.dtos.Usuarios.EdicionUsuarioDTO;
import co.edu.uniquindio.application.models.entitys.Usuario;

public class UsuarioMapper {

    public static Usuario toEntity(CreacionUsuarioDTO dto) {
        Usuario u = new Usuario();
        u.setNombre(dto.nombre());
        u.setEmail(dto.email());
        u.setContrasena(dto.contrasena());
        u.setTelefono(dto.telefono());
        u.setFoto(dto.foto());
        u.setFechaNacimiento(dto.fechaNacimiento());
        u.setRol(dto.rol());
        return u;
    }

    public static Usuario toEntity(EdicionUsuarioDTO dto) {
        Usuario u = new Usuario();
        u.setId(dto.id());
        u.setNombre(dto.nombre());
        u.setTelefono(dto.telefono());
        u.setFoto(dto.foto());
        u.setRol(dto.rol());
        return u;
    }

    public static UsuarioDTO toDTO(Usuario u) {
        if (u == null) return null;
        return new UsuarioDTO(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getTelefono(),
                u.getFoto(),
                u.getFechaNacimiento(),
                u.getRol()
        );
    }
}
