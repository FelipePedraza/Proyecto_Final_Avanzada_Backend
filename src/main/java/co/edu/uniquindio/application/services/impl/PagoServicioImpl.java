package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.config.StripeConfig;
import co.edu.uniquindio.application.dtos.pago.PagoIntentDTO;
import co.edu.uniquindio.application.services.PagoServicio;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.PaymentIntentCaptureParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoServicioImpl implements PagoServicio {

    private final StripeConfig stripeConfig;
    private final MeterRegistry meterRegistry;
    private final AtomicLong pagosTotalCentavos = new AtomicLong(0);

    @Override
    public PagoIntentDTO crearIntentPago(long montoCentavos, Long reservaId, String emailCliente)
            throws Exception {
        try {
            java.util.Map<String, String> metadata = new java.util.HashMap<>();
            metadata.put("reserva_id", String.valueOf(reservaId));
            metadata.put("plataforma", "ViviGo");

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(montoCentavos)
                    .setCurrency(stripeConfig.getCurrency())
                    .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                    .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.AUTOMATIC)
                    .addPaymentMethodType("card")
                    .setReceiptEmail(emailCliente)
                    .putAllMetadata(metadata)
                    .setDescription("Reserva ViviGo #" + reservaId)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            log.info("PaymentIntent creado: {} para reserva: {} - Estado inicial: {}",
                    paymentIntent.getId(), reservaId, paymentIntent.getStatus());

            return new PagoIntentDTO(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    montoCentavos,
                    stripeConfig.getCurrency()
            );

        } catch (StripeException e) {
            meterRegistry.counter("pagos.fallidos").increment();
            log.error("Error creando PaymentIntent para reserva {}: {} - Codigo: {}",
                    reservaId, e.getMessage(), e.getStripeError() != null ? e.getStripeError().getCode() : "N/A");
            throw new Exception("Error al inicializar el pago: " + e.getMessage(), e);
        }
    }

    @Override
    public void capturarPago(String paymentIntentId) throws Exception {
        if (paymentIntentId == null || paymentIntentId.trim().isEmpty()) {
            throw new IllegalArgumentException("El paymentIntentId no puede ser nulo o vacio");
        }

        try {
            log.info("Intentando capturar PaymentIntent: {}", paymentIntentId);

            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            String estadoActual = paymentIntent.getStatus();
            log.info("PaymentIntent {} - Estado actual: {}", paymentIntentId, estadoActual);

            if (!"requires_capture".equals(estadoActual)) {
                String mensajeError = String.format(
                        "El pago no esta en estado de captura. Estado actual: %s. " +
                        "Para capturar, el PaymentIntent debe estar en estado 'requires_capture'.",
                        estadoActual
                );
                log.error("{} - PaymentIntent: {}", mensajeError, paymentIntentId);
                throw new Exception(mensajeError);
            }

            Long amountCapturable = paymentIntent.getAmountCapturable();
            if (amountCapturable == null || amountCapturable <= 0) {
                throw new Exception("El PaymentIntent no tiene monto capturable disponible");
            }

            log.info("Capturando PaymentIntent {} - Monto capturable: {} centavos",
                    paymentIntentId, amountCapturable);

            PaymentIntentCaptureParams captureParams = PaymentIntentCaptureParams.builder()
                    .build();

            PaymentIntent capturedPaymentIntent = paymentIntent.capture(captureParams);

            log.info("Pago capturado exitosamente: {} - Estado final: {} - Monto capturado: {}",
                    paymentIntentId, capturedPaymentIntent.getStatus(), capturedPaymentIntent.getAmountReceived());

            meterRegistry.counter("pagos.exitosos").increment();
            pagosTotalCentavos.addAndGet(amountCapturable);
            meterRegistry.gauge("pagos.total", pagosTotalCentavos, AtomicLong::get);

        } catch (StripeException e) {
            meterRegistry.counter("pagos.fallidos").increment();
            String errorCode = e.getStripeError() != null ? e.getStripeError().getCode() : "unknown";
            String errorMessage = e.getStripeError() != null ? e.getStripeError().getMessage() : e.getMessage();

            log.error("Error capturando pago {} - Codigo: {} - Mensaje: {}",
                    paymentIntentId, errorCode, errorMessage);

            String mensajeUsuario = switch (errorCode) {
                case "payment_intent_unexpected_state" ->
                        "El pago no esta en un estado valido para captura: " + errorMessage;
                case "payment_intent_action_required" ->
                        "El pago requiere accion adicional (autenticacion 3D Secure)";
                case "payment_intent_payment_attempt_failed" ->
                        "El intento de pago fallo. El usuario debe reintentar.";
                case "resource_missing" ->
                        "El PaymentIntent no existe en Stripe";
                default -> "Error al capturar el pago: " + errorMessage;
            };

            throw new Exception(mensajeUsuario, e);
        } catch (Exception e) {
            log.error("Error inesperado capturando pago {}: {}", paymentIntentId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void cancelarPago(String paymentIntentId) throws Exception {
        if (paymentIntentId == null || paymentIntentId.trim().isEmpty()) {
            throw new IllegalArgumentException("El paymentIntentId no puede ser nulo o vacio");
        }

        try {
            log.info("Intentando cancelar PaymentIntent: {}", paymentIntentId);

            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            String estado = paymentIntent.getStatus();
            if ("succeeded".equals(estado)) {
                log.warn("PaymentIntent {} ya fue capturado. Usar reembolso en lugar de cancelacion.", paymentIntentId);
                throw new Exception("El pago ya fue capturado. Use el metodo de reembolso en lugar de cancelacion.");
            }

            if ("canceled".equals(estado)) {
                log.info("PaymentIntent {} ya estaba cancelado", paymentIntentId);
                return;
            }

            if (!"requires_capture".equals(estado) && !"requires_confirmation".equals(estado)
                    && !"requires_action".equals(estado)) {
                log.warn("No se puede cancelar PaymentIntent en estado {}: {}", estado, paymentIntentId);
                throw new Exception("No se puede cancelar el pago en estado: " + estado);
            }

            PaymentIntentCancelParams params = PaymentIntentCancelParams.builder()
                    .setCancellationReason(
                            PaymentIntentCancelParams.CancellationReason.ABANDONED
                    )
                    .build();

            PaymentIntent canceledPaymentIntent = paymentIntent.cancel(params);
            log.info("Pago cancelado: {} - Estado final: {}", paymentIntentId, canceledPaymentIntent.getStatus());

        } catch (StripeException e) {
            String errorCode = e.getStripeError() != null ? e.getStripeError().getCode() : "unknown";
            log.error("Error cancelando pago {} - Codigo: {} - Mensaje: {}",
                    paymentIntentId, errorCode, e.getMessage());
            throw new Exception("Error al cancelar el pago: " + e.getMessage(), e);
        }
    }

    @Override
    public void reembolsarPago(String paymentIntentId) throws Exception {
        if (paymentIntentId == null || paymentIntentId.trim().isEmpty()) {
            throw new IllegalArgumentException("El paymentIntentId no puede ser nulo o vacio");
        }

        try {
            log.info("Intentando reembolsar PaymentIntent: {}", paymentIntentId);

            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if (!"succeeded".equals(paymentIntent.getStatus())) {
                throw new Exception("No se puede reembolsar un pago que no ha sido capturado. Estado actual: "
                        + paymentIntent.getStatus());
            }

            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                    .build();

            Refund refund = Refund.create(params);
            log.info("Reembolso emitido: {} para PaymentIntent: {} - Monto: {} {} - Estado: {}",
                    refund.getId(), paymentIntentId, refund.getAmount(),
                    refund.getCurrency(), refund.getStatus());

        } catch (StripeException e) {
            String errorCode = e.getStripeError() != null ? e.getStripeError().getCode() : "unknown";
            log.error("Error reembolsando pago {} - Codigo: {} - Mensaje: {}",
                    paymentIntentId, errorCode, e.getMessage());
            throw new Exception("Error al reembolsar el pago: " + e.getMessage(), e);
        }
    }
}
