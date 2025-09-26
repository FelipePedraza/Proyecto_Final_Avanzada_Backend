package co.edu.uniquindio.application.mappers;

import co.edu.uniquindio.application.dtos.resena.CreacionResenaDTO;
import co.edu.uniquindio.application.dtos.resena.ItemResenaDTO;
import co.edu.uniquindio.application.models.entitys.Resena;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ResenaMapper {

    Resena toEntity(CreacionResenaDTO dto);

    ItemResenaDTO toItemDTO(Resena entity);

    void updateFromDto(CreacionResenaDTO dto, @MappingTarget Resena entity);

    default String mapRespuestaToString(co.edu.uniquindio.application.models.vo.Respuesta respuesta) {
        return respuesta != null ? respuesta.getMensaje() : null;
    }
}
