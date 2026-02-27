// ─── CreacionReservaRespuestaDTO.java ─────────────────────────────────────────
// Extiende la respuesta al crear reserva para incluir datos de pago.
// El frontend recibe tdo lo que necesita en una sola llamada.
package co.edu.uniquindio.application.dtos.reserva;

import co.edu.uniquindio.application.dtos.pago.PagoIntentDTO;

public record CreacionReservaRespuestaDTO(
        Long reservaId,
        double precioTotal,
        PagoIntentDTO pago
) {}