package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.PageResponseDTO;
import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.CreacionReservaRespuestaDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;
import co.edu.uniquindio.application.models.enums.ReservaEstado;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ReservaServicio {

    /**
     * Creates a new reservation with Stripe payment intent.
     *
     * @param dto the reservation creation data
     * @return response with payment intent details
     * @throws Exception if validation fails or accommodation not found
     */
    CreacionReservaRespuestaDTO crear(CreacionReservaDTO dto) throws Exception;

    /**
     * Accepts a pending reservation and captures the payment.
     *
     * @param id the reservation ID
     * @throws Exception if reservation not found or not in pending state
     */
    void aceptarReserva(Long id) throws Exception;

    /**
     * Rejects a pending reservation and cancels the payment.
     *
     * @param id the reservation ID
     * @throws Exception if reservation not found or not in pending state
     */
    void rechazarReserva(Long id) throws Exception;

    /**
     * Cancels a confirmed or pending reservation with refund if needed.
     *
     * @param id the reservation ID
     * @throws Exception if cancellation window has passed
     */
    void cancelarReserva(Long id) throws Exception;

    /**
     * Retrieves paginated reservations for a specific user with optional filters.
     *
     * @param id           the user ID
     * @param estado       optional filter by reservation state
     * @param fechaEntrada optional filter by start date
     * @param fechaSalida  optional filter by end date
     * @param pageable     the pagination information
     * @return paginated response with reservation items
     * @throws Exception if user not authenticated
     */
    PageResponseDTO<ItemReservaDTO> obtenerReservasUsuario(String id, ReservaEstado estado, LocalDate fechaEntrada, LocalDate fechaSalida, Pageable pageable) throws Exception;

    /**
     * Retrieves paginated reservations for a specific accommodation with optional filters.
     *
     * @param id           the accommodation ID
     * @param estado       optional filter by reservation state
     * @param fechaEntrada optional filter by start date
     * @param fechaSalida  optional filter by end date
     * @param pageable     the pagination information
     * @return paginated response with reservation details
     * @throws Exception if accommodation not found
     */
    PageResponseDTO<ReservaDTO> obtenerReservasAlojamiento(Long id, ReservaEstado estado, LocalDate fechaEntrada, LocalDate fechaSalida, Pageable pageable) throws Exception;
}
