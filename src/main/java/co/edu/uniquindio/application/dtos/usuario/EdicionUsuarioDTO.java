package co.edu.uniquindio.application.dtos.usuario;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record EdicionUsuarioDTO(
        @NotBlank
        String nombre,
        @NotBlank
        String telefono,
        String foto,
        String descripcion,
        List<String> documentos
) {
}