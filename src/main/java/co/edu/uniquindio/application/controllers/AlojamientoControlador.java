package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.alojamiento.*;
import co.edu.uniquindio.application.dtos.resena.CreacionResenaDTO;
import co.edu.uniquindio.application.dtos.resena.ItemResenaDTO;
import co.edu.uniquindio.application.services.AlojamientoServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alojamientos")
@RequiredArgsConstructor
public class AlojamientoControlador {

    private final AlojamientoServicio alojamientoServicio;

    @PostMapping
    public ResponseEntity<RespuestaDTO<AlojamientoDTO>> crearAlojamiento(@Valid @RequestBody CreacionAlojamientoDTO dto) throws Exception {
        AlojamientoDTO alojamiento = alojamientoServicio.crearAlojamiento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RespuestaDTO<>(false, alojamiento));
    }

    @GetMapping
    public ResponseEntity<RespuestaDTO<Page<ItemAlojamientoDTO>>> buscarAlojamientos(@ParameterObject Pageable pageable, @ParameterObject AlojamientoFiltro filtro) throws Exception {
        Page<ItemAlojamientoDTO> alojamientos = alojamientoServicio.buscarAlojamientos(pageable, filtro);
        return ResponseEntity.ok(new RespuestaDTO<>(false, alojamientos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaDTO<AlojamientoDTO>> obtenerAlojamiento(@PathVariable Long id) throws Exception {
        AlojamientoDTO alojamiento = alojamientoServicio.obtenerAlojamiento(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, alojamiento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaDTO<AlojamientoDTO>> editarAlojamiento(@PathVariable Long id, @Valid @RequestBody EdicionAlojamientoDTO dto) throws Exception {
        AlojamientoDTO alojamiento = alojamientoServicio.editarAlojamiento(id, dto);
        return ResponseEntity.ok(new RespuestaDTO<>(false, alojamiento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaDTO<String>> eliminarAlojamiento(@PathVariable Long id) throws Exception {
        alojamientoServicio.eliminarAlojamiento(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sugerencias")
    public ResponseEntity<RespuestaDTO<BusquedaCiudadDTO>> sugerirCiudades(@RequestParam String q) throws Exception {
        BusquedaCiudadDTO sugerencias = alojamientoServicio.sugerirCiudades(q);
        return ResponseEntity.ok(new RespuestaDTO<>(false, sugerencias));
    }

    @GetMapping("/{id}/metricas")
    public ResponseEntity<RespuestaDTO<MetricasDTO>> obtenerMetricas(
            @PathVariable Long id,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) throws Exception {
        MetricasDTO metricas = alojamientoServicio.obtenerMetricas(id, fechaInicio, fechaFin);
        return ResponseEntity.ok(new RespuestaDTO<>(false, metricas));
    }

    @GetMapping("/{id}/comentarios")
    public ResponseEntity<RespuestaDTO<Page<ItemResenaDTO>>> listarComentarios(
            @PathVariable Long id,
            @ParameterObject Pageable pageable) throws Exception {
        Page<ItemResenaDTO> comentarios = alojamientoServicio.listarComentarios(id, pageable);
        return ResponseEntity.ok(new RespuestaDTO<>(false, comentarios));
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<RespuestaDTO<ItemResenaDTO>> crearComentario(@PathVariable Long id, @Valid @RequestBody CreacionResenaDTO dto) throws Exception {
        ItemResenaDTO comentario = alojamientoServicio.crearComentario(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RespuestaDTO<>(false, comentario));
    }
}
