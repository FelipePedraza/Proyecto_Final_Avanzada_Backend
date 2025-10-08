package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;

import java.util.List;

public interface ReservaServicio {
    void crear(CreacionReservaDTO dto) throws Exception;
    void aceptarReserva(Long id) throws Exception;
    void rechazarReserva(Long id) throws Exception;
    void cancelarReserva(Long id) throws Exception;
}
