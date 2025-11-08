package co.edu.uniquindio.application.dtos.usuario;

public record TokenDTO(
        String token,
        String refreshToken
) {
}