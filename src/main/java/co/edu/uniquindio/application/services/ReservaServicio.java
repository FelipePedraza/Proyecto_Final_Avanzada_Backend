package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;

import java.util.List;

public interface ReservaServicio {
    void crear(CreacionReservaDTO dto) throws Exception;
    List<ItemReservaDTO> listarReservas(Long id, String estado, String fechaInicio, String fechaFin, int pagina) throws Exception;
    void cancelarReserva(Long id) throws Exception;
}
