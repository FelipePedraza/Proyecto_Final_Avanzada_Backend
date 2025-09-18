package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservaServicio {
    ReservaDTO crearReserva(CreacionReservaDTO dto) throws Exception;
    Page<ItemReservaDTO> listarReservas(Long id, String estado, String fechaInicio, String fechaFin, Pageable pageable) throws Exception;
    void cancelarReserva(Long id) throws Exception;
}
