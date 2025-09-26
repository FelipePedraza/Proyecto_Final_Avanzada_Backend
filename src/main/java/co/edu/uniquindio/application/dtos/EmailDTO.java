package co.edu.uniquindio.application.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailDTO(
	@NotBlank @Email String destinatario,
	@NotBlank String asunto,
	@NotBlank String mensaje
) {
}
