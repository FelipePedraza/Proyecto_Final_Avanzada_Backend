package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.alojamiento.*;
import co.edu.uniquindio.application.models.entitys.Alojamiento;

import java.util.List;

public interface AlojamientoServicio {
    void crear(CreacionAlojamientoDTO dto) throws Exception;
    void editar(Long id, EdicionAlojamientoDTO edicionAlojamientoDTO) throws Exception;
    void eliminar(Long id) throws Exception;
    Alojamiento obtenerAlojamientoId(Long id) throws Exception;
    MetricasDTO obtenerMetricas(Long id) throws Exception;
    List<ItemAlojamientoDTO> obtenerAlojamiento(AlojamientoFiltroDTO filtros) throws Exception;
    List<ItemAlojamientoDTO> obtenerAlojamientoUsuario(String id, int pagina) throws Exception;
}
