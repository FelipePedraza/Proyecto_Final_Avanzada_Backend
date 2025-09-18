package co.edu.uniquindio.application.dtos.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

public record MensajeDTO(
        @NotNull
        Integer remitenteId,
        @NotNull
        Integer destinatarioId,
        @NotNull
        Integer reservaId,
        @NotBlank @Length(max = 1000)
        String contenido,
        LocalDateTime fechaEnvio,
        boolean leido
) {
}
