package co.edu.uniquindio.application.mappers;

import co.edu.uniquindio.application.dtos.chat.ChatDTO;
import co.edu.uniquindio.application.dtos.chat.MensajeDTO;
import co.edu.uniquindio.application.models.entitys.Chat;
import co.edu.uniquindio.application.models.entitys.Mensaje;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ChatMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "usuario1", source = "usuario1")
    @Mapping(target = "usuario2", source = "usuario2")
    @Mapping(target = "mensajes", source = "mensajes")
    @Mapping(target = "ultimoMensaje", expression = "java(getUltimoMensaje(chat.getMensajes()))")
    @Mapping(target = "creadoEn", source = "creadoEn")
    @Mapping(target = "activo", source = "activo")
    ChatDTO toDTO(Chat chat);

    default MensajeDTO getUltimoMensaje(List<Mensaje> mensajes) {
        if (mensajes == null || mensajes.isEmpty()) {
            return null;
        }
        
        Mensaje ultimoMensaje = mensajes.stream()
            .max((m1, m2) -> m1.getFechaEnvio().compareTo(m2.getFechaEnvio()))
            .orElse(null);
            
        if (ultimoMensaje == null) {
            return null;
        }
        
        return new MensajeDTO(
            ultimoMensaje.getId(),
            ultimoMensaje.getRemitente().getId(),
            ultimoMensaje.getDestinatario().getId(),
            ultimoMensaje.getChat().getId(),
            ultimoMensaje.getContenido(),
            ultimoMensaje.getFechaEnvio(),
            ultimoMensaje.isLeido()
        );
    }
}
