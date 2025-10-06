package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.alojamiento.*;
import co.edu.uniquindio.application.dtos.usuario.CreacionAnfitrionDTO;
import co.edu.uniquindio.application.services.AlojamientoServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaDTO<AlojamientoDTO>> obtenerAlojamiento(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(new RespuestaDTO<>(false, alojamientoServicio.obtenerPorId(id)));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<RespuestaDTO<String>> editarAlojamiento(@PathVariable Long id, @RequestPart("alojamiento") @Valid EdicionAlojamientoDTO dto, @RequestPart(value = "imagenes", required = false) MultipartFile[] imagenes) throws Exception {
        alojamientoServicio.editar(id, dto, imagenes);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Se actualizo correctamente el alojamiento"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaDTO<String>> eliminarAlojamiento(@PathVariable Long id) throws Exception {
        alojamientoServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sugerencias")
    public ResponseEntity<RespuestaDTO<BusquedaCiudadDTO>> sugerirCiudades(@RequestParam String ciudad) throws Exception {
        return ResponseEntity.ok(new RespuestaDTO<>(false, null));
    }

    @GetMapping("/{id}/metricas")
    public ResponseEntity<RespuestaDTO<MetricasDTO>> obtenerMetricas(@PathVariable Long id) throws Exception {

        return ResponseEntity.ok(new RespuestaDTO<>(false, null));
    }

    @GetMapping
    public ResponseEntity<RespuestaDTO<List<ItemAlojamientoDTO>>> obtenerAlojamientos(@Valid AlojamientoFiltroDTO filtros, @RequestParam(defaultValue = "0") int pagina) throws Exception {
        return ResponseEntity.ok(new RespuestaDTO<>(false, alojamientoServicio.obtenerAlojamientos(filtros, pagina)));
    }
}
