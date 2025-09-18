package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.PaginacionDTO;
import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;
import co.edu.uniquindio.application.services.ReservaServicio;
import org.springframework.stereotype.Service;

@Service
public class ReservaServicioImpl implements ReservaServicio {

    @Override
    public ReservaDTO crearReserva(CreacionReservaDTO dto) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public PaginacionDTO<ItemReservaDTO> listarReservas(Long id, String estado, String fechaInicio, String fechaFin, int pagina, int tamano) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public void cancelarReserva(Long id) throws Exception {
        // Lógica de negocio a implementar
    }
}
