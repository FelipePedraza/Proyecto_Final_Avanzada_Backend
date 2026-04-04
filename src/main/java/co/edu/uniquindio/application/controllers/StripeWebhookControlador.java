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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookControlador {

    private final StripeConfig stripeConfig;
    private final ReservaRepositorio reservaRepositorio;

    // Set thread-safe para trackear eventos ya procesados (idempotencia básica)
    // En producción de alta escala, considerar usar Redis o base de datos
    private static final Set<String> PROCESSED_EVENTS = ConcurrentHashMap.newKeySet();

    @PostMapping("/webhook")
    public ResponseEntity<String> manejarWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.error("Firma del webhook invalida: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Firma invalida");
        } catch (Exception e) {
            log.error("Error construyendo evento del webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error procesando webhook");
        }

        // Idempotencia: verificar si el evento ya fue procesado
        if (PROCESSED_EVENTS.contains(event.getId())) {
            log.info("Evento {} ya fue procesado previamente (idempotencia)", event.getId());
            return ResponseEntity.ok("Evento ya procesado");
        }

        log.info("Webhook recibido: tipo={} id={}", event.getType(), event.getId());

        // Intentar deserializar con el SDK
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Optional<StripeObject> stripeObjectOptional = deserializer.getObject();

        try {
            switch (event.getType()) {

                case "payment_intent.amount_capturable_updated" -> {
                    PaymentIntent paymentIntent = obtenerPaymentIntent(stripeObjectOptional, payload);
                    if (paymentIntent != null) {
                        // Verificar explicitamente que este en estado "requires_capture"
                        // antes de marcar como AUTORIZADO
                        if ("requires_capture".equals(paymentIntent.getStatus())) {
                            log.info("PaymentIntent {} tiene monto capturable: {} centavos",
                                    paymentIntent.getId(), paymentIntent.getAmountCapturable());
                            manejarPagoAutorizadoPorId(paymentIntent.getId());
                        } else {
                            log.info("PaymentIntent {} recibido amount_capturable_updated pero estado es: {} - no se requiere accion",
                                    paymentIntent.getId(), paymentIntent.getStatus());
                        }
                    } else {
                        log.error("No se pudo obtener PaymentIntent del evento amount_capturable_updated");
                    }
                }

                case "payment_intent.succeeded" -> {
                    PaymentIntent paymentIntent = obtenerPaymentIntent(stripeObjectOptional, payload);
                    if (paymentIntent != null) {
                        log.info("Pago capturado exitosamente (succeeded): {}", paymentIntent.getId());
                        manejarPagoCapturadoPorId(paymentIntent.getId());
                    } else {
                        String id = extraerIdDelJson(payload);
                        if (id != null) {
                            manejarPagoCapturadoPorId(id);
                        } else {
                            log.error("No se pudo obtener paymentIntentId del evento succeeded");
                        }
                    }
                }

                case "payment_intent.payment_failed" -> {
                    PaymentIntent paymentIntent = obtenerPaymentIntent(stripeObjectOptional, payload);
                    if (paymentIntent != null) {
                        log.warn("Pago fallido (payment_failed): {}, codigo: {}, mensaje: {}",
                                paymentIntent.getId(),
                                paymentIntent.getLastPaymentError() != null ? paymentIntent.getLastPaymentError().getCode() : "N/A",
                                paymentIntent.getLastPaymentError() != null ? paymentIntent.getLastPaymentError().getMessage() : "N/A");
                        manejarPagoFallidoPorId(paymentIntent.getId());
                    } else {
                        String id = extraerIdDelJson(payload);
                        if (id != null) {
                            manejarPagoFallidoPorId(id);
                        } else {
                            log.error("No se pudo obtener paymentIntentId del evento payment_failed");
                        }
                    }
                }

                case "payment_intent.canceled" -> {
                    PaymentIntent paymentIntent = obtenerPaymentIntent(stripeObjectOptional, payload);
                    if (paymentIntent != null) {
                        manejarPagoCanceladoPorId(paymentIntent.getId());
                    } else {
                        String id = extraerIdDelJson(payload);
                        if (id != null) {
                            manejarPagoCanceladoPorId(id);
                        } else {
                            log.error("No se pudo obtener paymentIntentId del evento canceled");
                        }
                    }
                }

                case "charge.succeeded" ->
                        log.info("charge.succeeded recibido (evento relacionado, ya manejado por payment_intent.succeeded)");

                default -> log.debug("Evento no manejado: {}", event.getType());
            }

            // Marcar evento como procesado para idempotencia
            PROCESSED_EVENTS.add(event.getId());
            // Limpiar cache si crece demasiado (opcional, mantener ultimos 10000 eventos)
            if (PROCESSED_EVENTS.size() > 10000) {
                PROCESSED_EVENTS.clear();
                log.info("Cache de eventos procesados limpiado");
            }

            return ResponseEntity.ok("Evento procesado");

        } catch (Exception e) {
            log.error("Error procesando evento {}: {}", event.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno procesando evento");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Obtiene el PaymentIntent del evento, usando fallback a JSON si es necesario.
     * Retorna null si no se puede obtener.
     */
    private PaymentIntent obtenerPaymentIntent(Optional<StripeObject> stripeObjectOptional, String payload) {
        if (stripeObjectOptional.isPresent()) {
            return (PaymentIntent) stripeObjectOptional.get();
        }

        // Fallback: extraer ID del JSON y recuperar de Stripe
        String id = extraerIdDelJson(payload);
        if (id != null) {
            try {
                return PaymentIntent.retrieve(id);
            } catch (Exception e) {
                log.error("Error recuperando PaymentIntent {} de Stripe: {}", id, e.getMessage());
            }
        }
        return null;
    }

    private void manejarPagoAutorizadoPorId(String paymentIntentId) {
        try {
            reservaRepositorio.findByStripePaymentIntentId(paymentIntentId).ifPresentOrElse(
                    reserva -> {
                        // Solo actualizar si esta en estado PENDIENTE
                        if (reserva.getPagoEstado() == PagoEstado.PENDIENTE) {
                            reserva.setPagoEstado(PagoEstado.AUTORIZADO);
                            reservaRepositorio.save(reserva);
                            log.info("Reserva {} → AUTORIZADO (esperando anfitrion)", reserva.getId());
                        } else {
                            log.info("Reserva {} ya no esta en estado PENDIENTE (estado actual: {}), no se actualiza",
                                    reserva.getId(), reserva.getPagoEstado());
                        }
                    },
                    () -> log.warn("No se encontro reserva para PaymentIntent: {}", paymentIntentId)
            );
        } catch (Exception e) {
            log.error("Error manejando pago autorizado para {}: {}", paymentIntentId, e.getMessage(), e);
            throw e;
        }
    }

    private void manejarPagoCapturadoPorId(String paymentIntentId) {
        try {
            reservaRepositorio.findByStripePaymentIntentId(paymentIntentId).ifPresentOrElse(
                    reserva -> {
                        // Actualizar a CAPTURADO solo si estaba AUTORIZADO
                        if (reserva.getPagoEstado() == PagoEstado.AUTORIZADO) {
                            reserva.setPagoEstado(PagoEstado.CAPTURADO);
                            reservaRepositorio.save(reserva);
                            log.info("Reserva {} → CAPTURADO (pago completado)", reserva.getId());
                        } else if (reserva.getPagoEstado() != PagoEstado.CAPTURADO) {
                            log.warn("Reserva {} no puede pasar de {} a CAPTURADO", reserva.getId(), reserva.getPagoEstado());
                        } else {
                            log.info("Reserva {} ya estaba en estado CAPTURADO", reserva.getId());
                        }
                    },
                    () -> log.warn("No se encontro reserva para PaymentIntent capturado: {}", paymentIntentId)
            );
        } catch (Exception e) {
            log.error("Error manejando pago capturado para {}: {}", paymentIntentId, e.getMessage(), e);
            throw e;
        }
    }

    private void manejarPagoFallidoPorId(String paymentIntentId) {
        try {
            reservaRepositorio.findByStripePaymentIntentId(paymentIntentId).ifPresentOrElse(
                    reserva -> {
                        // No cancelar si ya fue capturado o reembolsado
                        if (reserva.getPagoEstado() != PagoEstado.CAPTURADO &&
                                reserva.getPagoEstado() != PagoEstado.REEMBOLSADO &&
                                reserva.getPagoEstado() != PagoEstado.CANCELADO) {
                            reserva.setPagoEstado(PagoEstado.CANCELADO);
                            reservaRepositorio.save(reserva);
                            log.info("Reserva {} → CANCELADO (pago fallido)", reserva.getId());
                        } else {
                            log.info("Reserva {} ya en estado final {}, no se modifica por pago fallido",
                                    reserva.getId(), reserva.getPagoEstado());
                        }
                    },
                    () -> log.warn("No se encontro reserva para PaymentIntent fallido: {}", paymentIntentId)
            );
        } catch (Exception e) {
            log.error("Error manejando pago fallido para {}: {}", paymentIntentId, e.getMessage(), e);
            throw e;
        }
    }

    private void manejarPagoCanceladoPorId(String paymentIntentId) {
        try {
            reservaRepositorio.findByStripePaymentIntentId(paymentIntentId).ifPresentOrElse(
                    reserva -> {
                        if (reserva.getPagoEstado() != PagoEstado.REEMBOLSADO &&
                                reserva.getPagoEstado() != PagoEstado.CAPTURADO) {
                            reserva.setPagoEstado(PagoEstado.CANCELADO);
                            reservaRepositorio.save(reserva);
                            log.info("Reserva {} → CANCELADO", reserva.getId());
                        } else {
                            log.info("Reserva {} ya en estado {}, no se cancela",
                                    reserva.getId(), reserva.getPagoEstado());
                        }
                    },
                    () -> log.warn("No se encontro reserva para PaymentIntent cancelado: {}", paymentIntentId)
            );
        } catch (Exception e) {
            log.error("Error manejando pago cancelado para {}: {}", paymentIntentId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Extrae el ID del objeto directamente del JSON cuando el SDK de Stripe
     * no puede deserializar (version de API del evento ≠ version del SDK).
     *
     * Estructura del payload:
     * { "data": { "object": { "id": "pi_xxx", ... } } }
     */
    private String extraerIdDelJson(String payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(payload);
            String id = root.path("data").path("object").path("id").asText(null);
            log.info("ID extraido del JSON crudo: {}", id);
            return id;
        } catch (Exception e) {
            log.error("Error extrayendo ID del JSON: {}", e.getMessage());
            return null;
        }
    }
}
