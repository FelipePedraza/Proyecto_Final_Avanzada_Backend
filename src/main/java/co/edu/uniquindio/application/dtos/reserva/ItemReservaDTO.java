package co.edu.uniquindio.application.dtos.reserva;

import co.edu.uniquindio.application.dtos.alojamiento.ItemAlojamientoDTO;
import co.edu.uniquindio.application.models.enums.ReservaEstado;

import java.time.LocalDate;

public record ItemReservaDTO(
        Long id,
        ItemAlojamientoDTO alojamiento,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        ReservaEstado estado
) {
}