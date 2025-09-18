package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;
import co.edu.uniquindio.application.services.ReservaServicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReservaServicioImpl implements ReservaServicio {

    @Override
    public ReservaDTO crearReserva(CreacionReservaDTO dto) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public Page<ItemReservaDTO> listarReservas(Long id, String estado, String fechaInicio, String fechaFin, Pageable pageable) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public void cancelarReserva(Long id) throws Exception {
        // Lógica de negocio a implementar
    }
}