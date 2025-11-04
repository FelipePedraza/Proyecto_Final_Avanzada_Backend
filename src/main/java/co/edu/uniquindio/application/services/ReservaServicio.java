package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;
import co.edu.uniquindio.application.models.enums.ReservaEstado;

import java.time.LocalDate;
import java.util.List;

public interface ReservaServicio {
    void crear(CreacionReservaDTO dto) throws Exception;
    void aceptarReserva(Long id) throws Exception;
    void rechazarReserva(Long id) throws Exception;
    void cancelarReserva(Long id) throws Exception;
    List<ItemReservaDTO> obtenerReservasUsuario(String id, ReservaEstado estado, LocalDate fechaEntrada, LocalDate fechaSalida, int pagina) throws Exception;
    List<ReservaDTO> obtenerReservasAlojamiento(Long id, ReservaEstado estado, LocalDate fechaEntrada, LocalDate fechaSalida, int pagina) throws Exception;
}
