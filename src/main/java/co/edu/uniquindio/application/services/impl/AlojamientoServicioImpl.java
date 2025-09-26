package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.alojamiento.*;
import co.edu.uniquindio.application.dtos.resena.CreacionResenaDTO;
import co.edu.uniquindio.application.dtos.resena.ItemResenaDTO;
import co.edu.uniquindio.application.dtos.usuario.EdicionUsuarioDTO;
import co.edu.uniquindio.application.models.entitys.Alojamiento;
import co.edu.uniquindio.application.repositories.AlojamientoRepositorio;
import co.edu.uniquindio.application.services.AlojamientoServicio;
import co.edu.uniquindio.application.mappers.AlojamientoMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlojamientoServicioImpl implements AlojamientoServicio {

    private final AlojamientoRepositorio alojamientoRepositorio;
    private final AlojamientoMapper alojamientoMapper;

    public AlojamientoServicioImpl(AlojamientoRepositorio alojamientoRepositorio, AlojamientoMapper alojamientoMapper) {
        this.alojamientoRepositorio = alojamientoRepositorio;
        this.alojamientoMapper = alojamientoMapper;
    }

    @Override
    public void crear(CreacionAlojamientoDTO alojamientoDTO) throws Exception {
        Alojamiento entity = alojamientoMapper.toEntity(alojamientoDTO);
        alojamientoRepositorio.save(entity);
    }

    @Override
    public void editar(Long id, EdicionAlojamientoDTO edicionAlojamientoDTO) throws Exception {
        Alojamiento alojamiento = obtenerAlojamientoId(id);
        alojamientoMapper.updateAlojamientoFromDto(edicionAlojamientoDTO, alojamiento);
        alojamientoRepositorio.save(alojamiento);
    }

    @Override
    public void eliminar(Long id) throws Exception {
        Alojamiento alojamiento = obtenerAlojamientoId(id);
        // Aquí podrías cambiar el estado o eliminar físicamente
        alojamientoRepositorio.delete(alojamiento);
    }

    @Override
    public Alojamiento obtenerAlojamientoId(Long id) throws Exception {
        return alojamientoRepositorio.findById(id)
                .orElseThrow(() -> new Exception("Alojamiento no encontrado"));
    }

    @Override
    public MetricasDTO obtenerMetricas(Long id) throws Exception {
        return null;
    }

    @Override
    public List<ItemAlojamientoDTO> obtenerAlojamiento(AlojamientoFiltroDTO filtros) throws Exception {
        // Ejemplo: obtener todos y mapear a DTO
        List<Alojamiento> alojamientos = alojamientoRepositorio.findAll();
        return alojamientos.stream()
                .map(alojamientoMapper::toItemDTO)
                .toList();
    }

    @Override
    public List<ItemAlojamientoDTO> obtenerAlojamientoUsuario(String id, int pagina) throws Exception {

        Pageable pageable = PageRequest.of(pagina, 5);
        Page<ItemAlojamientoDTO> alojamientos = alojamientoRepositorio.getAlojamientos(id, pageable);

        return alojamientos.toList();
    }
}