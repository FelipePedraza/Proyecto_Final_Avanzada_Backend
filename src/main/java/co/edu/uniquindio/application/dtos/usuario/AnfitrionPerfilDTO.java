package co.edu.uniquindio.application.dtos.usuario;

import java.util.List;

public record AnfitrionPerfilDTO(
        String descripcion,
        List<String> documentos
) {
}