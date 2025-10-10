package co.edu.uniquindio.application.mappers;

import co.edu.uniquindio.application.dtos.chat.MensajeDTO;
import co.edu.uniquindio.application.models.entitys.Mensaje;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MensajeMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "remitenteId", source = "remitente.id")
    @Mapping(target = "destinatarioId", source = "destinatario.id")
    @Mapping(target = "chatId", source = "chat.id")
    @Mapping(target = "contenido", source = "contenido")
    @Mapping(target = "fechaEnvio", source = "fechaEnvio")
    @Mapping(target = "leido", source = "leido")
    MensajeDTO toDTO(Mensaje mensaje);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaEnvio", ignore = true)
    @Mapping(target = "leido", ignore = true)
    @Mapping(target = "remitente", ignore = true)
    @Mapping(target = "destinatario", ignore = true)
    @Mapping(target = "chat", ignore = true)
    Mensaje toEntity(MensajeDTO mensajeDTO);
}
