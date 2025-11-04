package co.edu.uniquindio.application.mappers;

import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;
import co.edu.uniquindio.application.models.entitys.Reserva;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReservaMapper {

    @Mapping(target = "estado", constant = "PENDIENTE")
    @Mapping(target = "creadoEn", expression = "java(java.time.LocalDateTime.now())")

    Reserva toEntity(CreacionReservaDTO reservaDTO);

    ItemReservaDTO toItemDTO(Reserva reserva);

    Reserva toEntity(ReservaDTO reservaDTO);

    ReservaDTO toDTO(Reserva reserva);

}
