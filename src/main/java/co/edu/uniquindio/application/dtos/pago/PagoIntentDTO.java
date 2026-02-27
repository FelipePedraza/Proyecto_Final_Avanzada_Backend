// ─── PagoIntentDTO.java ───────────────────────────────────────────────────────
// Respuesta al crear una reserva: contiene lo que el frontend necesita
// para completar el pago con Stripe.js o el SDK móvil de Stripe.
package co.edu.uniquindio.application.dtos.pago;

public record PagoIntentDTO(
        /**
         * El client_secret del PaymentIntent.
         * El frontend lo usa con stripe.confirmCardPayment(clientSecret, ...)
         * NUNCA loguear ni exponer este valor en producción más de lo necesario.
         */
        String clientSecret,

        /**
         * ID del PaymentIntent (pi_XXXXXXX).
         * El frontend puede usarlo para rastrear el estado.
         */
        String paymentIntentId,

        /**
         * El monto exacto que se cobrará, en la unidad de la moneda.
         * Ejemplo: 25000 = $250.00 USD o $25,000 COP.
         */
        Long montoCentavos,

        /**
         * Moneda en formato ISO 4217 (usd, cop, etc.)
         */
        String moneda
) {}