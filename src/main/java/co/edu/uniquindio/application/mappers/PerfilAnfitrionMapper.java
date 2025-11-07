package co.edu.uniquindio.application.mappers;
import co.edu.uniquindio.application.dtos.usuario.AnfitrionPerfilDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import co.edu.uniquindio.application.dtos.usuario.CreacionAnfitrionDTO;
import co.edu.uniquindio.application.models.entitys.PerfilAnfitrion;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PerfilAnfitrionMapper {

    PerfilAnfitrion toEntity(CreacionAnfitrionDTO creacionAnfitrionDTO);

    AnfitrionPerfilDTO toAnfitrionPerfilDTO(PerfilAnfitrion perfilAnfitrion);

}
