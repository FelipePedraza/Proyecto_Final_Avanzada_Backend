package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.PageResponseDTO;
import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.alojamiento.*;
import co.edu.uniquindio.application.dtos.resena.CreacionResenaDTO;
import co.edu.uniquindio.application.dtos.resena.CreacionRespuestaDTO;
import co.edu.uniquindio.application.dtos.resena.ItemResenaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;
import co.edu.uniquindio.application.models.enums.ReservaEstado;
import co.edu.uniquindio.application.services.AlojamientoServicio;
import co.edu.uniquindio.application.services.ResenaServicio;
import co.edu.uniquindio.application.services.ReservaServicio;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/alojamientos")
@RequiredArgsConstructor
public class AlojamientoControlador {

    private final AlojamientoServicio alojamientoServicio;
    private final ReservaServicio reservaServicio;
    private final ResenaServicio resenaServicio;

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_PAGE = 0;

    @Timed(value = "vivigo.api.alojamiento", description = "Alojamiento API timing")
    @PostMapping
    public ResponseEntity<RespuestaDTO<String>> crearAlojamiento(@RequestBody @Valid CreacionAlojamientoDTO dto) throws Exception {
        alojamientoServicio.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RespuestaDTO<>(false, "Alojamiento creado con exito"));
    }

    @Timed(value = "vivigo.api.alojamiento", description = "Alojamiento API timing")
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaDTO<AlojamientoDTO>> obtenerAlojamiento(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(new RespuestaDTO<>(false, alojamientoServicio.obtenerPorId(id)));
    }

    @Timed(value = "vivigo.api.alojamiento", description = "Alojamiento API timing")
    @PutMapping(value = "/{id}")
    public ResponseEntity<RespuestaDTO<String>> editarAlojamiento(@PathVariable Long id, @RequestBody @Valid EdicionAlojamientoDTO dto) throws Exception {
        alojamientoServicio.editar(id, dto);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Se actualizo correctamente el alojamiento"));
    }

    @Timed(value = "vivigo.api.alojamiento", description = "Alojamiento API timing")
    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaDTO<String>> eliminarAlojamiento(@PathVariable Long id) throws Exception {
        alojamientoServicio.eliminar(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Se elimino con exito el alojamiento"));
    }

    @Timed(value = "vivigo.api.alojamiento", description = "Alojamiento API timing")
    @GetMapping("/sugerencias")
    public ResponseEntity<RespuestaDTO<PageResponseDTO<ItemAlojamientoDTO>>> sugerirCiudades(
            @RequestParam(required = false) String ciudad,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String ciudadLimpia = (ciudad == null || "undefined".equalsIgnoreCase(ciudad.trim()))
                ? null
                : ciudad.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by("promedioCalificaciones").descending());
        PageResponseDTO<ItemAlojamientoDTO> alojamientos = alojamientoServicio.sugerirAlojamientos(ciudadLimpia, pageable);
        return ResponseEntity.ok(new RespuestaDTO<>(false, alojamientos));
    }

    @Timed(value = "vivigo.api.alojamiento", description = "Alojamiento API timing")
    @GetMapping("/{id}/metricas")
    public ResponseEntity<RespuestaDTO<MetricasDTO>> obtenerMetricas(@PathVariable Long id) throws Exception {
        MetricasDTO metricas = alojamientoServicio.obtenerMetricas(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, metricas));
    }

    @Timed(value = "vivigo.api.alojamiento", description = "Alojamiento API timing")
    @GetMapping
    public ResponseEntity<RespuestaDTO<PageResponseDTO<ItemAlojamientoDTO>>> obtenerAlojamientos(
            @Valid AlojamientoFiltroDTO filtros,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "creadoEn,desc") String[] sort) throws Exception {

        Sort sorting = Sort.by("creadoEn").descending();
        if (sort != null && sort.length > 0 && sort[0].contains(",")) {
            String[] sortParts = sort[0].split(",");
            String sortField = sortParts[0];
            String sortDirection = sortParts.length > 1 ? sortParts[1] : "desc";
            sorting = sortDirection.equalsIgnoreCase("asc")
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
        }

        Pageable pageable = PageRequest.of(page, size, sorting);
        PageResponseDTO<ItemAlojamientoDTO> resultado = alojamientoServicio.obtenerAlojamientos(filtros, pageable);
        return ResponseEntity.ok(new RespuestaDTO<>(false, resultado));
    }

    @Timed(value = "vivigo.api.alojamiento", description = "Alojamiento API timing")
    @GetMapping("/{id}/reservas")
    public ResponseEntity<RespuestaDTO<PageResponseDTO<ReservaDTO>>> obtenerReservasAlojamiento(
            @PathVariable(value = "id") Long id,
            @RequestParam(required = false) ReservaEstado estado,
            @RequestParam(required = false) LocalDate fechaEntrada,
            @RequestParam(required = false) LocalDate fechaSalida,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaEntrada,desc") String[] sort) throws Exception {

        Sort sorting = Sort.by("fechaEntrada").descending();
        if (sort != null && sort.length > 0 && sort[0].contains(",")) {
            String[] sortParts = sort[0].split(",");
            String sortField = sortParts[0];
            String sortDirection = sortParts.length > 1 ? sortParts[1] : "desc";
            sorting = sortDirection.equalsIgnoreCase("asc")
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
        }

        Pageable pageable = PageRequest.of(page, size, sorting);
        PageResponseDTO<ReservaDTO> reservas = reservaServicio.obtenerReservasAlojamiento(id, estado, fechaEntrada, fechaSalida, pageable);
        return ResponseEntity.ok(new RespuestaDTO<>(false, reservas));
    }

    @Timed(value = "vivigo.api.alojamiento", description = "Alojamiento API timing")
    @GetMapping("/{id}/resenas")
    public ResponseEntity<RespuestaDTO<PageResponseDTO<ItemResenaDTO>>> obtenerResenasAlojamiento(
            @PathVariable(value = "id") Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) throws Exception {

        Pageable pageable = PageRequest.of(page, size, Sort.by("creadoEn").descending());
        PageResponseDTO<ItemResenaDTO> resenas = resenaServicio.obtenerResenasAlojamiento(id, pageable);
        return ResponseEntity.ok(new RespuestaDTO<>(false, resenas));
    }

    @Timed(value = "vivigo.api.alojamiento", description = "Alojamiento API timing")
    @PostMapping("/{id}/resenas")
    public ResponseEntity<RespuestaDTO<String>> crearResena(@PathVariable(value = "id")  Long id, @RequestBody @Valid CreacionResenaDTO dto) throws Exception {
        resenaServicio.crear(id, dto);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Se creo correctamente el resena al alojamiento"));
    }

    @Timed(value = "vivigo.api.alojamiento", description = "Alojamiento API timing")
    @PostMapping("/{id}/resenas/{idResena}/respuesta")
    public ResponseEntity<RespuestaDTO<String>> crearRespuesta(@PathVariable(value = "idResena")  Long id, @RequestBody @Valid CreacionRespuestaDTO dto) throws Exception {
        resenaServicio.responder(id, dto);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Se creo correctamente la respuesta a la reseña"));
    }
}
