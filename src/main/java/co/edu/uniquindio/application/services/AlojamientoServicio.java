package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.PageResponseDTO;
import co.edu.uniquindio.application.dtos.alojamiento.*;
import org.springframework.data.domain.Pageable;

public interface AlojamientoServicio {
    void crear(CreacionAlojamientoDTO dto) throws Exception;
    void editar(Long id, EdicionAlojamientoDTO edicionAlojamientoDTO) throws Exception;
    void eliminar(Long id) throws Exception;
    AlojamientoDTO obtenerPorId(Long id) throws Exception;
    MetricasDTO obtenerMetricas(Long id) throws Exception;

    /**
     * Retrieves paginated list of accommodations with filters.
     *
     * @param filtros  the filter criteria
     * @param pageable the pagination information (page, size, sort)
     * @return paginated response with accommodation items
     */
    PageResponseDTO<ItemAlojamientoDTO> obtenerAlojamientos(AlojamientoFiltroDTO filtros, Pageable pageable) throws Exception;

    /**
     * Retrieves paginated list of accommodations for a specific user.
     *
     * @param id       the user ID
     * @param pageable the pagination information (page, size, sort)
     * @return paginated response with accommodation items
     */
    PageResponseDTO<ItemAlojamientoDTO> obtenerAlojamientosUsuario(String id, Pageable pageable) throws Exception;

    /**
     * Retrieves paginated list of suggested accommodations by city.
     *
     * @param ciudad   the city name
     * @param pageable the pagination information (page, size, sort)
     * @return paginated response with accommodation items
     */
    PageResponseDTO<ItemAlojamientoDTO> sugerirAlojamientos(String ciudad, Pageable pageable);
}
