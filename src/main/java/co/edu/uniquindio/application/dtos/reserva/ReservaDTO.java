package co.edu.uniquindio.application.dtos.reserva;

import co.edu.uniquindio.application.dtos.alojamiento.AlojamientoDTO;
import co.edu.uniquindio.application.dtos.usuario.UsuarioDTO;
import co.edu.uniquindio.application.models.enums.ReservaEstado;

import java.time.LocalDate;

public record ReservaDTO(
        Long id,
        AlojamientoDTO alojamiento,
        UsuarioDTO usuario,
        LocalDate fechaEntrada,
        LocalDate fechaSalida,
        Integer numeroHuespedes,
        ReservaEstado estado
) {
}