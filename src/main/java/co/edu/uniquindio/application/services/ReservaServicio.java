package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.PaginacionDTO;
import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;

public interface ReservaServicio {
    ReservaDTO crearReserva(CreacionReservaDTO dto) throws Exception;
    PaginacionDTO<ItemReservaDTO> listarReservas(Long id, String estado, String fechaInicio, String fechaFin, int pagina, int tamano) throws Exception;
    void cancelarReserva(Long id) throws Exception;
}