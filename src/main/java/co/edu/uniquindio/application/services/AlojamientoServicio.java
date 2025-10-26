package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.alojamiento.*;

import java.util.List;

public interface AlojamientoServicio {
    void crear(CreacionAlojamientoDTO dto) throws Exception;
    void editar (Long id, EdicionAlojamientoDTO edicionAlojamientoDTO) throws Exception;
    void eliminar(Long id) throws Exception;
    AlojamientoDTO obtenerPorId(Long id) throws Exception;
    MetricasDTO obtenerMetricas(Long id) throws Exception;
    List<ItemAlojamientoDTO> obtenerAlojamientos(AlojamientoFiltroDTO filtros, int pagina) throws Exception;
    List<ItemAlojamientoDTO> obtenerAlojamientosUsuario(String id, int pagina) throws Exception;
    List<ItemAlojamientoDTO> sugerirAlojamientos(String ciudad);
}
