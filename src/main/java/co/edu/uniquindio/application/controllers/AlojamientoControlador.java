package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.alojamiento.*;
import co.edu.uniquindio.application.dtos.usuario.EdicionUsuarioDTO;
import co.edu.uniquindio.application.models.entitys.Alojamiento;
import co.edu.uniquindio.application.services.AlojamientoServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alojamientos")
@RequiredArgsConstructor
public class AlojamientoControlador {

    private final AlojamientoServicio alojamientoServicio;

    @PostMapping
    public ResponseEntity<RespuestaDTO<String>> crearAlojamiento(@Valid @RequestBody CreacionAlojamientoDTO dto) throws Exception {
        alojamientoServicio.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RespuestaDTO<>(false, "Alojamiento creado exitosamente"));
    }

    @GetMapping
    public ResponseEntity<RespuestaDTO<Page<ItemAlojamientoDTO>>> buscarAlojamientos(@ParameterObject Pageable pageable, @ParameterObject AlojamientoFiltroDTO filtro) throws Exception {
        List<ItemAlojamientoDTO> alojamientos = alojamientoServicio.obtenerAlojamiento(filtro);
        Page<ItemAlojamientoDTO> page = new PageImpl<>(alojamientos, pageable, alojamientos.size());
        return ResponseEntity.ok(new RespuestaDTO<>(false, page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaDTO<Alojamiento>> obtenerAlojamiento(@PathVariable Long id) throws Exception {
        Alojamiento alojamiento = alojamientoServicio.obtenerAlojamientoId(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, alojamiento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaDTO<String>> editarAlojamiento(@PathVariable Long id, @Valid @RequestBody EdicionAlojamientoDTO dto) throws Exception {
        
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Método de edición no implementado"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaDTO<String>> eliminarAlojamiento(@PathVariable Long id) throws Exception {
        alojamientoServicio.eliminar(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Alojamiento eliminado exitosamente"));
    }

    @GetMapping("/{id}/metricas")
    public ResponseEntity<RespuestaDTO<MetricasDTO>> obtenerMetricas(@PathVariable Long id) throws Exception {
        MetricasDTO metricasDTO = alojamientoServicio.obtenerMetricas(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, metricasDTO));
    }
}
