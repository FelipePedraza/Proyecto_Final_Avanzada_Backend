package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.config.StripeConfig;
import co.edu.uniquindio.application.models.enums.PagoEstado;
import co.edu.uniquindio.application.repositories.ReservaRepositorio;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookControlador {

    private final StripeConfig stripeConfig;
    private final ReservaRepositorio reservaRepositorio;

    @PostMapping("/webhook")
    public ResponseEntity<String> manejarWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.error("Firma del webhook inválida: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Firma inválida");
        }

        log.info("Webhook recibido: tipo={} id={}", event.getType(), event.getId());

        // Intentar deserializar con el SDK
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Optional<StripeObject> stripeObjectOptional = deserializer.getObject();

        // IMPORTANTE: si el SDK no puede deserializar (versión de API del evento
        // distinta a la del SDK), usamos el fallback de Jackson para extraer el ID
        // directamente del JSON crudo. Nunca retornamos "Evento ignorado".

        switch (event.getType()) {

            case "payment_intent.amount_capturable_updated" -> {
                // Usuario pagó exitosamente → autorizar reserva
                if (stripeObjectOptional.isPresent()) {
                    manejarPagoAutorizadoPorId(((PaymentIntent) stripeObjectOptional.get()).getId());
                } else {
                    String id = extraerIdDelJson(payload);
                    if (id != null) manejarPagoAutorizadoPorId(id);
                    else log.error("No se pudo obtener paymentIntentId del evento amount_capturable_updated");
                }
            }

            case "payment_intent.succeeded" -> {
                String id = stripeObjectOptional.isPresent()
                        ? ((PaymentIntent) stripeObjectOptional.get()).getId()
                        : extraerIdDelJson(payload);
                log.info("Pago capturado (succeeded): {}", id);
            }

            case "payment_intent.canceled" -> {
                if (stripeObjectOptional.isPresent()) {
                    manejarPagoCanceladoPorId(((PaymentIntent) stripeObjectOptional.get()).getId());
                } else {
                    String id = extraerIdDelJson(payload);
                    if (id != null) manejarPagoCanceladoPorId(id);
                    else log.error("No se pudo obtener paymentIntentId del evento canceled");
                }
            }

            case "charge.succeeded" ->
                    log.info("charge.succeeded recibido");

            default -> log.debug("Evento no manejado: {}", event.getType());
        }

        return ResponseEntity.ok("Evento procesado");
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void manejarPagoAutorizadoPorId(String paymentIntentId) {
        reservaRepositorio.findByStripePaymentIntentId(paymentIntentId).ifPresentOrElse(
                reserva -> {
                    reserva.setPagoEstado(PagoEstado.AUTORIZADO);
                    reservaRepositorio.save(reserva);
                    log.info("Reserva {} → AUTORIZADO (esperando anfitrión)", reserva.getId());
                },
                () -> log.warn("No se encontró reserva para PaymentIntent: {}", paymentIntentId)
        );
    }

    private void manejarPagoCanceladoPorId(String paymentIntentId) {
        reservaRepositorio.findByStripePaymentIntentId(paymentIntentId).ifPresentOrElse(
                reserva -> {
                    if (reserva.getPagoEstado() != PagoEstado.REEMBOLSADO) {
                        reserva.setPagoEstado(PagoEstado.CANCELADO);
                        reservaRepositorio.save(reserva);
                        log.info("Reserva {} → CANCELADO", reserva.getId());
                    }
                },
                () -> log.warn("No se encontró reserva para PaymentIntent cancelado: {}", paymentIntentId)
        );
    }

    /**
     * Extrae el ID del objeto directamente del JSON cuando el SDK de Stripe
     * no puede deserializar (versión de API del evento ≠ versión del SDK).
     *
     * Estructura del payload:
     * { "data": { "object": { "id": "pi_xxx", ... } } }
     */
    private String extraerIdDelJson(String payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(payload);
            String id = root.path("data").path("object").path("id").asText(null);
            log.info("ID extraído del JSON crudo: {}", id);
            return id;
        } catch (Exception e) {
            log.error("Error extrayendo ID del JSON: {}", e.getMessage());
            return null;
        }
    }
}