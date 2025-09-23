package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.alojamiento.*;
import co.edu.uniquindio.application.dtos.resena.CreacionResenaDTO;
import co.edu.uniquindio.application.dtos.resena.ItemResenaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AlojamientoServicio {
    AlojamientoDTO crearAlojamiento(CreacionAlojamientoDTO dto) throws Exception;
    Page<ItemAlojamientoDTO> buscarAlojamientos(Pageable pageable, AlojamientoFiltro filtro) throws Exception;
    AlojamientoDTO obtenerAlojamiento(Long id) throws Exception;
    AlojamientoDTO editarAlojamiento(Long id, EdicionAlojamientoDTO dto) throws Exception;
    void eliminarAlojamiento(Long id) throws Exception;
    BusquedaCiudadDTO sugerirCiudades(String q) throws Exception;
    MetricasDTO obtenerMetricas(Long id, String fechaInicio, String fechaFin) throws Exception;
    Page<ItemResenaDTO> listarComentarios(Long id, Pageable pageable) throws Exception;
    ItemResenaDTO crearComentario(Long id, CreacionResenaDTO dto) throws Exception;
}
