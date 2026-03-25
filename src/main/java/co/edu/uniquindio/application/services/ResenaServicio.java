package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.PageResponseDTO;
import co.edu.uniquindio.application.dtos.resena.CreacionResenaDTO;
import co.edu.uniquindio.application.dtos.resena.CreacionRespuestaDTO;
import co.edu.uniquindio.application.dtos.resena.ItemResenaDTO;
import org.springframework.data.domain.Pageable;

public interface ResenaServicio {

    /**
     * Creates a new review for an accommodation.
     * Updates the average rating of the accommodation.
     *
     * @param idAlojamiento the accommodation ID
     * @param dto           the review creation data
     * @throws Exception if validation fails or accommodation not found
     */
    void crear(Long idAlojamiento, CreacionResenaDTO dto) throws Exception;

    /**
     * Responds to an existing review.
     *
     * @param resenaId the review ID
     * @param dto      the response data
     * @throws Exception if review not found or already responded
     */
    void responder(Long resenaId, CreacionRespuestaDTO dto) throws Exception;

    /**
     * Retrieves paginated reviews for a specific accommodation.
     *
     * @param alojamientoId the accommodation ID
     * @param pageable      the pagination information
     * @return paginated response with review items
     * @throws Exception if accommodation not found
     */
    PageResponseDTO<ItemResenaDTO> obtenerResenasAlojamiento(Long alojamientoId, Pageable pageable) throws Exception;

}
