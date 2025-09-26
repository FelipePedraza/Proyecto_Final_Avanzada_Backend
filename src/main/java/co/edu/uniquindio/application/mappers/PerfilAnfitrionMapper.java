package co.edu.uniquindio.application.mappers;

import co.edu.uniquindio.application.dtos.usuario.AnfitrionPerfilDTO;
import co.edu.uniquindio.application.dtos.usuario.CreacionAnfitrionDTO;
import co.edu.uniquindio.application.models.entitys.PerfilAnfitrion;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PerfilAnfitrionMapper {


    PerfilAnfitrion toEntity(CreacionAnfitrionDTO dto);

    AnfitrionPerfilDTO toDTO(PerfilAnfitrion entity);

    void updateFromDto(CreacionAnfitrionDTO dto, @MappingTarget PerfilAnfitrion entity);

}
