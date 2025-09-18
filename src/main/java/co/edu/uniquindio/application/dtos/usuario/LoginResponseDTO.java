package co.edu.uniquindio.application.dtos.usuario;

public record LoginResponseDTO(
        String token,
        UsuarioDTO usuario,
        Integer expiresIn
) {
}
