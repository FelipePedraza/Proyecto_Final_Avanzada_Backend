package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.pago.PagoIntentDTO;

public interface PagoServicio {
    /**
     * Crea un PaymentIntent en Stripe con captura manual (manual capture).
     * Esto autoriza la tarjeta del usuario SIN cobrarle todavía.
     * El dinero solo se transfiere cuando se llame a capturarPago().
     *
     * @param montoCentavos Monto en centavos (USD) o unidad mínima (COP en centavos)
     * @param reservaId     ID de la reserva, se guarda en los metadatos de Stripe para rastreabilidad
     * @param emailCliente  Email del cliente (opcional, aparece en el dashboard de Stripe)
     */
    PagoIntentDTO crearIntentPago(long montoCentavos, Long reservaId, String emailCliente) throws Exception;

    /**
     * Captura un PaymentIntent previamente autorizado.
     * Se llama cuando el anfitrión acepta la reserva.
     * A partir de aquí, Stripe transfiere el dinero.
     */
    void capturarPago(String paymentIntentId) throws Exception;

    /**
     * Cancela un PaymentIntent autorizado pero NO capturado.
     * Se usa cuando el anfitrión rechaza la reserva o el usuario cancela
     * una reserva PENDIENTE. No se cobra nada al usuario.
     */
    void cancelarPago(String paymentIntentId) throws Exception;

    /**
     * Emite un reembolso total sobre un PaymentIntent ya capturado.
     * Se usa cuando el usuario cancela una reserva CONFIRMADA dentro
     * de la política de cancelación.
     */
    void reembolsarPago(String paymentIntentId) throws Exception;
}