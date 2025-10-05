package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.alojamiento.*;
import co.edu.uniquindio.application.services.AlojamientoServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/alojamientos")
@RequiredArgsConstructor
public class AlojamientoControlador {

    private final AlojamientoServicio alojamientoServicio;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<RespuestaDTO<String>> crearAlojamiento(@RequestPart("alojamiento") @Valid CreacionAlojamientoDTO dto, @RequestPart(value = "imagenes", required = false) MultipartFile[] imagenes) throws Exception {
        alojamientoServicio.crear(dto, imagenes);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RespuestaDTO<>(false, "Alojamiento creado con exito"));
    }

    @GetMapping
    public ResponseEntity<RespuestaDTO<Page<ItemAlojamientoDTO>>> buscarAlojamientos(@ParameterObject Pageable pageable, @ParameterObject AlojamientoFiltroDTO filtro) throws Exception {

        return ResponseEntity.ok(new RespuestaDTO<>(false, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaDTO<AlojamientoDTO>> obtenerAlojamiento(@PathVariable Long id) throws Exception {
        alojamientoServicio.obtenerAlojamientoId(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaDTO<AlojamientoDTO>> editarAlojamiento(@PathVariable Long id, @Valid @RequestBody EdicionAlojamientoDTO dto) throws Exception {

        return ResponseEntity.ok(new RespuestaDTO<>(false, null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaDTO<String>> eliminarAlojamiento(@PathVariable Long id) throws Exception {

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sugerencias")
    public ResponseEntity<RespuestaDTO<BusquedaCiudadDTO>> sugerirCiudades(@RequestParam String q) throws Exception {
        return ResponseEntity.ok(new RespuestaDTO<>(false, null));
    }

    @GetMapping("/{id}/metricas")
    public ResponseEntity<RespuestaDTO<MetricasDTO>> obtenerMetricas(@PathVariable Long id, @RequestParam String fechaInicio, @RequestParam String fechaFin) throws Exception {

        return ResponseEntity.ok(new RespuestaDTO<>(false, null));
    }
}
