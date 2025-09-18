package co.edu.uniquindio.application.dtos.reserva;

import co.edu.uniquindio.application.models.enums.ReservaEstado;
import jakarta.validation.constraints.NotNull;

public record EstadoReservaDTO(
        @NotNull
        ReservaEstado estado
) {
}