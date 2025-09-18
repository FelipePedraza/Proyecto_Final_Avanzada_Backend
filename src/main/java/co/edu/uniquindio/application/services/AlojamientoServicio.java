package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.PaginacionDTO;
import co.edu.uniquindio.application.dtos.alojamiento.*;
import co.edu.uniquindio.application.dtos.resena.CreacionResenaDTO;
import co.edu.uniquindio.application.dtos.resena.ItemResenaDTO;

import java.util.List;

public interface AlojamientoServicio {
    AlojamientoDTO crearAlojamiento(CreacionAlojamientoDTO dto) throws Exception;
    PaginacionDTO<ItemAlojamientoDTO> buscarAlojamientos(AlojamientoFiltro filtro) throws Exception;
    AlojamientoDTO obtenerAlojamiento(Long id) throws Exception;
    AlojamientoDTO editarAlojamiento(Long id, EditarAlojamientoDTO dto) throws Exception;
    void eliminarAlojamiento(Long id) throws Exception;
    BusquedaCiudadDTO sugerirCiudades(String q) throws Exception;
    MetricasDTO obtenerMetricas(Long id, String fechaInicio, String fechaFin) throws Exception;
    PaginacionDTO<ItemResenaDTO> listarComentarios(Long id, int pagina, int tamano) throws Exception;
    ItemResenaDTO crearComentario(Long id, CreacionResenaDTO dto) throws Exception;
}