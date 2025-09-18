package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;
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
    public ResponseEntity<RespuestaDTO<ReservaDTO>> crearReserva(@Valid @RequestBody CreacionReservaDTO dto) throws Exception {
        ReservaDTO reserva = reservaServicio.crearReserva(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RespuestaDTO<>(false, reserva));
    }

    @GetMapping
    public ResponseEntity<RespuestaDTO<PaginacionDTO<ItemReservaDTO>>> listarReservas(
            @RequestParam Long id,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) throws Exception {
        PaginacionDTO<ItemReservaDTO> reservas = reservaServicio.listarReservas(id, estado, fechaInicio, fechaFin, pagina, tamano);
        return ResponseEntity.ok(new RespuestaDTO<>(false, reservas));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<RespuestaDTO<String>> cancelarReserva(@PathVariable Long id) throws Exception {
        reservaServicio.cancelarReserva(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Reserva cancelada correctamente."));
    }
}