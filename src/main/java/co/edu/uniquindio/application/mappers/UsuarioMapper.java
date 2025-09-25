package co.edu.uniquindio.application.mappers;

import co.edu.uniquindio.application.dtos.usuario.CreacionUsuarioDTO;
import co.edu.uniquindio.application.dtos.usuario.EdicionUsuarioDTO;
import co.edu.uniquindio.application.dtos.usuario.UsuarioDTO;
import co.edu.uniquindio.application.models.entitys.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UsuarioMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "estado", constant = "ACTIVO")
    @Mapping(target = "creadoEn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "rol", constant = "Huesped")

    Usuario toEntity(CreacionUsuarioDTO userDTO);

    UsuarioDTO toUserDTO(Usuario user);

    void updateUsuarioFromDTO(EdicionUsuarioDTO dto, @MappingTarget Usuario usuario);

}
