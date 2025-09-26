package co.edu.uniquindio.application.mappers;

import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;
import co.edu.uniquindio.application.models.entitys.Reserva;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReservaMapper {


    Reserva toEntity(CreacionReservaDTO dto);

    ItemReservaDTO toItemDTO(Reserva entity);

    ReservaDTO toDTO(Reserva entity);

    void updateFromDto(CreacionReservaDTO dto, @MappingTarget Reserva entity);

}
