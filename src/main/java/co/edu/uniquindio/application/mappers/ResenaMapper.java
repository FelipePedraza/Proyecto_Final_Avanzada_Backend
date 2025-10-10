package co.edu.uniquindio.application.mappers;

import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.resena.CreacionResenaDTO;
import co.edu.uniquindio.application.dtos.resena.CreacionRespuestaDTO;
import co.edu.uniquindio.application.dtos.resena.ItemResenaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;
import co.edu.uniquindio.application.models.entitys.Resena;
import co.edu.uniquindio.application.models.vo.Respuesta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ResenaMapper {

    @Mapping(target = "creadoEn", expression = "java(java.time.LocalDateTime.now())")
    Resena toEntity(CreacionResenaDTO resenaDTO);

    Resena toEntity(ReservaDTO reservaDTO);

    Respuesta toEntity(CreacionRespuestaDTO respuestaDTO);

}
