package co.edu.uniquindio.application.mappers;

import co.edu.uniquindio.application.models.entitys.ContrasenaCodigoReinicio;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContrasenaCodigoReinicioMapper {


    ContrasenaCodigoReinicio toEntity(String codigo);

    void updateFromCodigo(String codigo, @MappingTarget ContrasenaCodigoReinicio entity);

}
