package co.edu.uniquindio.application.models.enums;

/**
 * Refleja el ciclo de vida del PaymentIntent de Stripe dentro del sistema.
 *
 * Flujo normal:
 *   PENDIENTE → AUTORIZADO → CAPTURADO
 *
 * Flujo de cancelación/rechazo:
 *   PENDIENTE → CANCELADO
 *   AUTORIZADO → REEMBOLSADO
 */
public enum PagoEstado {
    /**
     * El PaymentIntent fue creado pero el usuario aún no ha completado
     * el pago en el frontend (no ha ingresado su tarjeta).
     */
    PENDIENTE,

    /**
     * El usuario completó el pago. La tarjeta fue autorizada pero el
     * dinero aún NO fue transferido al anfitrión. Stripe lo llama
     * "requires_capture" (autorización sin captura inmediata).
     * Corresponde a reserva en estado PENDIENTE esperando al anfitrión.
     */
    AUTORIZADO,

    /**
     * El anfitrión aceptó la reserva y el dinero fue capturado.
     * Corresponde a reserva CONFIRMADA.
     */
    CAPTURADO,

    /**
     * El pago fue cancelado antes de ser capturado (anfitrión rechazó
     * o usuario canceló mientras estaba PENDIENTE).
     * No se cobró nada al usuario.
     */
    CANCELADO,

    /**
     * El pago fue reembolsado después de haber sido capturado
     * (usuario canceló una reserva CONFIRMADA dentro de la política).
     */
    REEMBOLSADO
}