package co.edu.uniquindio.application.controllers;


import co.edu.uniquindio.application.dtos.alojamiento.ItemAlojamientoDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.usuario.EdicionUsuarioDTO;
import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.usuario.CambioContrasenaDTO;
import co.edu.uniquindio.application.dtos.usuario.UsuarioDTO;
import co.edu.uniquindio.application.services.UsuarioServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaDTO<UsuarioDTO>> editarPerfil(@PathVariable("id") Long id, @Valid @RequestBody EdicionUsuarioDTO dto) throws Exception {
        UsuarioDTO updated = usuarioServicio.editarPerfil(id, dto);
        return ResponseEntity.ok(new RespuestaDTO<>(false, updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaDTO<UsuarioDTO>> obtenerInformacion(@PathVariable("id") Long id) throws Exception {
        return ResponseEntity.ok(new RespuestaDTO<>(false, usuarioServicio.obtenerInformacion(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaDTO<String>> eliminarCuenta(@PathVariable("id") Long id) throws Exception {
        usuarioServicio.eliminarCuenta(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/contrasena")
    public ResponseEntity<RespuestaDTO<String>> cambiarContrasena(@PathVariable("id") Long id, @Valid @RequestBody CambioContrasenaDTO dto) throws Exception {
        usuarioServicio.cambiarContrasena(id, dto);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Contraseña cambiada correctamente"));
    }

    @GetMapping("/{id}/alojamientos")
    public ResponseEntity<RespuestaDTO<Page<ItemAlojamientoDTO>>> listarAlojamientos(
            @PathVariable("id") Long id,
            @ParameterObject Pageable pageable) throws Exception {
        Page<ItemAlojamientoDTO> alojamientos = usuarioServicio.listarAlojamientos(id, pageable);
        return ResponseEntity.ok(new RespuestaDTO<>(false, alojamientos));
    }

    @GetMapping("/{id}/reservas")
    public ResponseEntity<RespuestaDTO<Page<ItemReservaDTO>>> listarReservas(
            @PathVariable("id") Long id,
            @ParameterObject Pageable pageable) throws Exception {
        Page<ItemReservaDTO> reservas = usuarioServicio.listarReservas(id, pageable);
        return ResponseEntity.ok(new RespuestaDTO<>(false, reservas));
    }
}