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

import java.util.List;

@Mapper (componentModel = MappingConstants.ComponentModel.SPRING)
public interface AlojamientoMapper {

    @Mapping(target = "estado", constant = "ACTIVO")
    @Mapping(target = "creadoEn", expression = "java(java.time.LocalDateTime.now())")

    Alojamiento toEntity(CreacionAlojamientoDTO dto);

    @Mapping(target = "imagenPrincipal", expression = "java(getImagenPrincipal(alojamiento.getImagenes()))")
    ItemAlojamientoDTO toItemDTO(Alojamiento alojamiento);

    @Mapping(target = "nombreAnfitrion", expression = "java(alojamiento.getAnfitrion().getNombre())")
    @Mapping(target = "anfitrionId", expression = "java(alojamiento.getAnfitrion().getId())")
    AlojamientoDTO toDTO(Alojamiento alojamiento);

    void updateAlojamientoFromDto(EdicionAlojamientoDTO edicionAlojamientoDTO, @MappingTarget Alojamiento alojamiento);

    default String getImagenPrincipal(List<String> imagenes){
        return (imagenes == null || imagenes.isEmpty())  ? "" : imagenes.getFirst();
    }
}
