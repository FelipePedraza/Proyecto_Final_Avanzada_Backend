package co.edu.uniquindio.application.dtos.reserva;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreacionReservaDTO(
        @NotNull
        Long alojamientoId,
        @NotNull
        Long usuarioId,
        @NotNull @Future
        LocalDate fechaEntrada,
        @NotNull @Future
        LocalDate fechaSalida,
        @NotNull @Min(1)
        Integer numeroHuespedes
) {
}