package co.edu.uniquindio.application.mappers;

import co.edu.uniquindio.application.dtos.chat.ChatDTO;
import co.edu.uniquindio.application.dtos.chat.MensajeDTO;
import co.edu.uniquindio.application.models.entitys.Chat;
import co.edu.uniquindio.application.models.entitys.Mensaje;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ChatMapper {
    ChatMapper INSTANCE = Mappers.getMapper(ChatMapper.class);

    ChatDTO toDTO(Chat chat);
    MensajeDTO toMensajeDTO(Mensaje mensaje);
    Mensaje toEntity(MensajeDTO mensajeDTO);
    // Nuevo método para convertir Usuario a UsuarioDTO
    co.edu.uniquindio.application.dtos.usuario.UsuarioDTO toUsuarioDTO(co.edu.uniquindio.application.models.entitys.Usuario usuario);
}
