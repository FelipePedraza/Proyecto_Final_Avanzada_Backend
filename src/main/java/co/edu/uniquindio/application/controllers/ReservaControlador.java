package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.services.ReservaServicio;
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

    @PostMapping
    public ResponseEntity<RespuestaDTO<String>> crearReserva(@Valid @RequestBody CreacionReservaDTO dto) throws Exception {
        reservaServicio.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RespuestaDTO<>(false, "La reserva se creo con exito"));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<RespuestaDTO<String>> cancelarReserva(@PathVariable Long id) throws Exception {
        reservaServicio.cancelarReserva(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Reserva cancelada correctamente."));
    }

    @PatchMapping("/{id}/aceptar")
    public ResponseEntity<RespuestaDTO<String>> aceptarReserva(@PathVariable Long id) throws Exception {
        reservaServicio.aceptarReserva(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Reserva aceptada y confirmada"));
    }

    @PatchMapping("/{id}/rechazar")
    public ResponseEntity<RespuestaDTO<String>> rechazarReserva(@PathVariable Long id) throws Exception {
        reservaServicio.rechazarReserva(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Reserva ha sido rechazada"));
    }
}
