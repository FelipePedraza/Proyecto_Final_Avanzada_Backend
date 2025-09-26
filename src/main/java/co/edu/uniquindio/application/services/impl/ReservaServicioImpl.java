package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;
import co.edu.uniquindio.application.services.ReservaServicio;
import co.edu.uniquindio.application.mappers.ReservaMapper;
import co.edu.uniquindio.application.models.entitys.Reserva;
import co.edu.uniquindio.application.repositories.ReservaRepositorio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReservaServicioImpl implements ReservaServicio {

    private final ReservaRepositorio reservaRepositorio;
    private final ReservaMapper reservaMapper;

    public ReservaServicioImpl(ReservaRepositorio reservaRepositorio, ReservaMapper reservaMapper) {
        this.reservaRepositorio = reservaRepositorio;
        this.reservaMapper = reservaMapper;
    }

    @Override
    public ReservaDTO crearReserva(CreacionReservaDTO dto) throws Exception {
        Reserva reserva = reservaMapper.toEntity(dto);
        reservaRepositorio.save(reserva);
        return reservaMapper.toDTO(reserva);
    }

    @Override
    public Page<ItemReservaDTO> listarReservas(Long id, String estado, String fechaInicio, String fechaFin, Pageable pageable) throws Exception {
        // Ejemplo: obtener todas y mapear a DTO
        Page<Reserva> reservas = reservaRepositorio.findAll(pageable);
        return reservas.map(reservaMapper::toItemDTO);
    }

    @Override
    public void cancelarReserva(Long id) throws Exception {
        Reserva reserva = reservaRepositorio.findById(id)
                .orElseThrow(() -> new Exception("Reserva no encontrada"));
        // Aquí podrías cambiar el estado o eliminar físicamente
        reservaRepositorio.delete(reserva);
    }
}