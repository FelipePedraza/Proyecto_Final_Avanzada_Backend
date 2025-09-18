package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.alojamiento.*;
import co.edu.uniquindio.application.dtos.resena.CreacionResenaDTO;
import co.edu.uniquindio.application.dtos.resena.ItemResenaDTO;
import co.edu.uniquindio.application.services.AlojamientoServicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AlojamientoServicioImpl implements AlojamientoServicio {

    @Override
    public AlojamientoDTO crearAlojamiento(CreacionAlojamientoDTO dto) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public Page<ItemAlojamientoDTO> buscarAlojamientos(Pageable pageable, AlojamientoFiltro filtro) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public AlojamientoDTO obtenerAlojamiento(Long id) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public AlojamientoDTO editarAlojamiento(Long id, EditarAlojamientoDTO dto) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public void eliminarAlojamiento(Long id) throws Exception {
        // Lógica de negocio a implementar
    }

    @Override
    public BusquedaCiudadDTO sugerirCiudades(String q) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public MetricasDTO obtenerMetricas(Long id, String fechaInicio, String fechaFin) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public Page<ItemResenaDTO> listarComentarios(Long id, Pageable pageable) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public ItemResenaDTO crearComentario(Long id, CreacionResenaDTO dto) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }
}