package co.edu.uniquindio.application.mappers;


import co.edu.uniquindio.application.dtos.usuario.UsuarioDTO;
import co.edu.uniquindio.application.dtos.usuario.CreacionUsuarioDTO;
import co.edu.uniquindio.application.dtos.usuario.EdicionUsuarioDTO;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.models.enums.Estado;

import java.time.LocalDateTime;

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
        u.setCreadoEn(LocalDateTime.now());
        u.setEsAnfitrion(false);
        u.setEstado(Estado.valueOf("ACTIVO"));
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
                u.getRol(),
                u.getFechaNacimiento(),
                u.getFoto()
        );
    }


}
