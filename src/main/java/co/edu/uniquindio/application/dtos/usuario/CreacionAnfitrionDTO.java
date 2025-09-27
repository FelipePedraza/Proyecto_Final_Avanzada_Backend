package co.edu.uniquindio.application.dtos.usuario;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreacionAnfitrionDTO(
    @NotNull String sobreMi,
    @NotNull String DocumentoLegal
) {
}