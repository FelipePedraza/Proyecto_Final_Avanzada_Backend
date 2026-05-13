package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.CreacionReservaRespuestaDTO;
import co.edu.uniquindio.application.services.ReservaServicio;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaControlador {

    private final ReservaServicio reservaServicio;

    @Timed(value = "vivigo.api.reserva", description = "Reserva API timing")
    @PostMapping
    public ResponseEntity<RespuestaDTO<CreacionReservaRespuestaDTO>> crearReserva(
            @Valid @RequestBody CreacionReservaDTO dto) throws Exception {
        CreacionReservaRespuestaDTO respuesta = reservaServicio.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RespuestaDTO<>(false, respuesta));
    }

    @Timed(value = "vivigo.api.reserva", description = "Reserva API timing")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<RespuestaDTO<String>> cancelarReserva(@PathVariable Long id) throws Exception {
        reservaServicio.cancelarReserva(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Reserva cancelada correctamente."));
    }

    @Timed(value = "vivigo.api.reserva", description = "Reserva API timing")
    @PatchMapping("/{id}/aceptar")
    public ResponseEntity<RespuestaDTO<String>> aceptarReserva(@PathVariable Long id) throws Exception {
        reservaServicio.aceptarReserva(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Reserva aceptada y pago capturado"));
    }

    @Timed(value = "vivigo.api.reserva", description = "Reserva API timing")
    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<RespuestaDTO<String>> rechazarReserva(@PathVariable Long id) throws Exception {
        reservaServicio.rechazarReserva(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Reserva rechazada y pago cancelado"));
    }
}
