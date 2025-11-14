package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.alojamiento.*;
import co.edu.uniquindio.application.dtos.resena.CreacionResenaDTO;
import co.edu.uniquindio.application.dtos.resena.CreacionRespuestaDTO;
import co.edu.uniquindio.application.dtos.resena.ItemResenaDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;
import co.edu.uniquindio.application.models.enums.ReservaEstado;
import co.edu.uniquindio.application.services.AlojamientoServicio;
import co.edu.uniquindio.application.services.ResenaServicio;
import co.edu.uniquindio.application.services.ReservaServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/alojamientos")
@RequiredArgsConstructor
public class AlojamientoControlador {

    private final AlojamientoServicio alojamientoServicio;
    private final ReservaServicio reservaServicio;
    private final ResenaServicio resenaServicio;


    @PostMapping
    public ResponseEntity<RespuestaDTO<String>> crearAlojamiento(@RequestBody @Valid CreacionAlojamientoDTO dto) throws Exception {
        alojamientoServicio.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RespuestaDTO<>(false, "Alojamiento creado con exito"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaDTO<AlojamientoDTO>> obtenerAlojamiento(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(new RespuestaDTO<>(false, alojamientoServicio.obtenerPorId(id)));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<RespuestaDTO<String>> editarAlojamiento(@PathVariable Long id, @RequestBody @Valid EdicionAlojamientoDTO dto) throws Exception {
        alojamientoServicio.editar(id, dto);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Se actualizo correctamente el alojamiento"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaDTO<String>> eliminarAlojamiento(@PathVariable Long id) throws Exception {
        alojamientoServicio.eliminar(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Se elimino con exito el alojamiento"));
    }

    @GetMapping("/sugerencias")
    public ResponseEntity<RespuestaDTO<List<ItemAlojamientoDTO>>> sugerirCiudades(@RequestParam String ciudad) {
        List<ItemAlojamientoDTO> alojamientos = alojamientoServicio.sugerirAlojamientos(ciudad);
        return ResponseEntity.ok(new RespuestaDTO<>(false, alojamientos));
    }

    @GetMapping("/{id}/metricas")
    public ResponseEntity<RespuestaDTO<MetricasDTO>> obtenerMetricas(@PathVariable Long id) throws Exception {
        MetricasDTO metricas = alojamientoServicio.obtenerMetricas(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, metricas));
    }

    @GetMapping
    public ResponseEntity<RespuestaDTO<List<ItemAlojamientoDTO>>> obtenerAlojamientos(@Valid AlojamientoFiltroDTO filtros, @RequestParam(defaultValue = "0") int pagina) throws Exception {
        return ResponseEntity.ok(new RespuestaDTO<>(false, alojamientoServicio.obtenerAlojamientos(filtros, pagina)));
    }

    @GetMapping("/{id}/reservas")
    public ResponseEntity<RespuestaDTO<List<ReservaDTO>>> obtenerReservasAlojamiento(@PathVariable(value = "id")  Long id, @RequestParam(required = false) ReservaEstado estado, @RequestParam(required = false) LocalDate fechaEntrada, @RequestParam(required = false) LocalDate fechaSalida , @RequestParam(required = false, defaultValue = "0") int pagina) throws Exception {
        List<ReservaDTO> reservas = reservaServicio.obtenerReservasAlojamiento(id, estado, fechaEntrada, fechaSalida, pagina);
        return ResponseEntity.ok(new RespuestaDTO<>(false, reservas));
    }

    @GetMapping("/{id}/resenas")
    public ResponseEntity<RespuestaDTO<List<ItemResenaDTO>>> obtenerResenasAlojamiento(@PathVariable(value = "id")  Long id, @RequestParam(defaultValue = "0") int pagina) throws Exception {
        List<ItemResenaDTO> resenas = resenaServicio.obtenerResenasAlojamiento(id, pagina);
        return ResponseEntity.ok(new RespuestaDTO<>(false, resenas));
    }

    @PostMapping("/{id}/resenas")
    public ResponseEntity<RespuestaDTO<String>> crearResena(@PathVariable(value = "id")  Long id, @RequestBody @Valid CreacionResenaDTO dto) throws Exception {
        resenaServicio.crear(id, dto);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Se creo correctamente el resena al alojamiento"));
    }

    @PostMapping("/{id}/resenas/{idResena}/respuesta")
    public ResponseEntity<RespuestaDTO<String>> crearRespuesta(@PathVariable(value = "idResena")  Long id, @RequestBody @Valid CreacionRespuestaDTO dto) throws Exception {
        resenaServicio.responder(id, dto);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Se creo correctamente la respuesta a la reseña"));
    }


}
