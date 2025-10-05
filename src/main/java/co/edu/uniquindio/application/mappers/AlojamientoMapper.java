package co.edu.uniquindio.application.mappers;

import co.edu.uniquindio.application.dtos.alojamiento.AlojamientoDTO;
import co.edu.uniquindio.application.dtos.alojamiento.CreacionAlojamientoDTO;
import co.edu.uniquindio.application.dtos.alojamiento.EdicionAlojamientoDTO;
import co.edu.uniquindio.application.dtos.alojamiento.ItemAlojamientoDTO;
import co.edu.uniquindio.application.models.entitys.Alojamiento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper (componentModel = MappingConstants.ComponentModel.SPRING)
public interface AlojamientoMapper {

    @Mapping(target = "estado", constant = "ACTIVO")
    @Mapping(target = "creadoEn", expression = "java(java.time.LocalDateTime.now())")

    Alojamiento toEntity(CreacionAlojamientoDTO dto);

    ItemAlojamientoDTO toItemDTO(Alojamiento alojamiento);

    AlojamientoDTO toDTO(Alojamiento alojamiento);

    void updateAlojamientoFromDto(EdicionAlojamientoDTO edicionAlojamientoDTO, @MappingTarget Alojamiento alojamiento);
}
