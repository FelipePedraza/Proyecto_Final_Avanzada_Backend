package co.edu.uniquindio.application.dtos.reserva;

import co.edu.uniquindio.application.dtos.alojamiento.AlojamientoDTO;
import co.edu.uniquindio.application.models.enums.ReservaEstado;

import java.time.LocalDate;

public record ItemReservaDTO(
        Long id,
        AlojamientoDTO alojamiento,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        ReservaEstado estado
) {
}