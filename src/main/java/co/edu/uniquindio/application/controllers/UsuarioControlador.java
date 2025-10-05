package co.edu.uniquindio.application.controllers;


import co.edu.uniquindio.application.dtos.alojamiento.ItemAlojamientoDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.usuario.*;
import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.services.UsuarioServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;

import java.util.List;


@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    @PostMapping("/anfitrion")
    public ResponseEntity<RespuestaDTO<String>> crearAnfitrion(@Valid @RequestBody CreacionAnfitrionDTO dto) throws Exception {
        usuarioServicio.crearAnfitrion(dto);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Se ha creado el anfitrion"));
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<RespuestaDTO<String>> editar(@PathVariable String id, @RequestPart("usuario") @Valid EdicionUsuarioDTO edicionUsuarioDTO, @RequestPart(value = "foto", required = false) MultipartFile file) throws Exception {
        usuarioServicio.editar(id, edicionUsuarioDTO, file);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "El usuario ha sido actualizado"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaDTO<UsuarioDTO>> obtener(@PathVariable String id) throws Exception {
        UsuarioDTO usuarioDTO = usuarioServicio.obtener(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, usuarioDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaDTO<String>> eliminar(@PathVariable String id) throws Exception {
        usuarioServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/contrasena")
    public ResponseEntity<RespuestaDTO<String>> cambiarContrasena(@PathVariable String id, @Valid @RequestBody CambioContrasenaDTO dto) throws Exception {
        usuarioServicio.cambiarContrasena(id, dto);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "La contraseña ha sido cambiada"));
    }

    @GetMapping("/{id}/alojamientos")
    public ResponseEntity<RespuestaDTO<List<ItemAlojamientoDTO>>> obtenerAlojamientosUsuario(@PathVariable String id) throws Exception {
        return ResponseEntity.ok(new RespuestaDTO<>(false, List.of()));
    }

    @GetMapping("/{id}/reservas")
    public ResponseEntity<RespuestaDTO<List<ItemReservaDTO>>> obtenerReservasUsuario(@PathVariable String id) throws Exception {
        return ResponseEntity.ok(new RespuestaDTO<>(false, List.of()));
    }

}