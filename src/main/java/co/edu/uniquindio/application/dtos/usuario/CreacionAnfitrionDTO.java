package co.edu.uniquindio.application.dtos.usuario;


import jakarta.validation.constraints.NotBlank;

public record CreacionAnfitrionDTO(
    @NotBlank String usuarioId,
    @NotBlank String sobreMi,
    @NotBlank String documentoLegal
) {
}