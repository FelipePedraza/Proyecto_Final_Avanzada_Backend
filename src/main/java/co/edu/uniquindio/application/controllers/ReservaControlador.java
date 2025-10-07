package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;
import co.edu.uniquindio.application.services.ReservaServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<RespuestaDTO<List<ItemReservaDTO>>> listarReservas(
            @RequestParam Long id,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false, defaultValue = "0") int pagina,
            @ParameterObject Pageable pageable) throws Exception {
        List<ItemReservaDTO> reservas = reservaServicio.listarReservas(id, estado, fechaInicio, fechaFin, pagina);
        return ResponseEntity.ok(new RespuestaDTO<>(false, reservas));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<RespuestaDTO<String>> cancelarReserva(@PathVariable Long id) throws Exception {
        reservaServicio.cancelarReserva(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Reserva cancelada correctamente."));
    }
}
