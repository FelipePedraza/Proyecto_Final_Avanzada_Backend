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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoServicioImpl implements PagoServicio {

    private final StripeConfig stripeConfig;

    @Override
    public PagoIntentDTO crearIntentPago(long montoCentavos, Long reservaId, String emailCliente)
            throws Exception {
        try {
            // Metadata: información adicional visible en el dashboard de Stripe.
            // Muy útil para soporte y auditoría.
            Map<String, String> metadata = new HashMap<>();
            metadata.put("reserva_id", String.valueOf(reservaId));
            metadata.put("plataforma", "ViviGo");

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(montoCentavos)
                    .setCurrency(stripeConfig.getCurrency())
                    // CLAVE: capture_method = MANUAL significa que Stripe autoriza
                    // la tarjeta pero NO cobra hasta que llamemos a capture().
                    .setCaptureMethod(PaymentIntentCreateParams.CaptureMethod.MANUAL)
                    // Permite al frontend confirmar el pago con el client_secret
                    .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.AUTOMATIC)
                    // Métodos de pago aceptados
                    .addPaymentMethodType("card")
                    .setReceiptEmail(emailCliente)
                    .putAllMetadata(metadata)
                    // Descripción visible en el dashboard de Stripe
                    .setDescription("Reserva ViviGo #" + reservaId)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            log.info("PaymentIntent creado: {} para reserva: {}", paymentIntent.getId(), reservaId);

            return new PagoIntentDTO(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    montoCentavos,
                    stripeConfig.getCurrency()
            );

        } catch (StripeException e) {
            log.error("Error creando PaymentIntent para reserva {}: {}", reservaId, e.getMessage());
            throw new Exception("Error al inicializar el pago: " + e.getMessage(), e);
        }
    }

    @Override
    public void capturarPago(String paymentIntentId) throws Exception {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // Solo se puede capturar si está en estado "requires_capture"
            // (es decir, el usuario ya autorizó el pago con su tarjeta)
            if (!"requires_capture".equals(paymentIntent.getStatus())) {
                throw new Exception(
                        "El pago no está en estado de captura. Estado actual: " + paymentIntent.getStatus()
                );
            }

            paymentIntent.capture(PaymentIntentCaptureParams.builder().build());
            log.info("Pago capturado exitosamente: {}", paymentIntentId);

        } catch (StripeException e) {
            log.error("Error capturando pago {}: {}", paymentIntentId, e.getMessage());
            throw new Exception("Error al capturar el pago: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelarPago(String paymentIntentId) throws Exception {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // No cancelar si ya fue capturado (en ese caso usar reembolsarPago)
            String estado = paymentIntent.getStatus();
            if ("succeeded".equals(estado) || "canceled".equals(estado)) {
                log.warn("Intento de cancelar PaymentIntent en estado {}: {}", estado, paymentIntentId);
                return;
            }

            PaymentIntentCancelParams params = PaymentIntentCancelParams.builder()
                    .setCancellationReason(
                            PaymentIntentCancelParams.CancellationReason.ABANDONED
                    )
                    .build();

            paymentIntent.cancel(params);
            log.info("Pago cancelado: {}", paymentIntentId);

        } catch (StripeException e) {
            log.error("Error cancelando pago {}: {}", paymentIntentId, e.getMessage());
            throw new Exception("Error al cancelar el pago: " + e.getMessage(), e);
        }
    }

    @Override
    public void reembolsarPago(String paymentIntentId) throws Exception {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    // Reembolso total. Para parcial: .setAmount(centavos)
                    .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                    .build();

            Refund refund = Refund.create(params);
            log.info("Reembolso emitido: {} para PaymentIntent: {}", refund.getId(), paymentIntentId);

        } catch (StripeException e) {
            log.error("Error reembolsando pago {}: {}", paymentIntentId, e.getMessage());
            throw new Exception("Error al reembolsar el pago: " + e.getMessage(), e);
        }
    }
}