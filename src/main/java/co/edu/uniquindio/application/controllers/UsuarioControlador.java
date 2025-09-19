package co.edu.uniquindio.application.controllers;


import co.edu.uniquindio.application.dtos.usuario.CreacionUsuarioDTO;
import co.edu.uniquindio.application.dtos.usuario.EdicionUsuarioDTO;
import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.usuario.CambioContrasenaDTO;
import co.edu.uniquindio.application.dtos.usuario.UsuarioDTO;
import co.edu.uniquindio.application.services.UsuarioServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Validated
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    @PostMapping
    public ResponseEntity<RespuestaDTO<UsuarioDTO>> crear(@Valid @RequestBody CreacionUsuarioDTO dto) throws Exception {
        UsuarioDTO created = usuarioServicio.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RespuestaDTO<>(false, created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaDTO<UsuarioDTO>> editar(@PathVariable("id") Long id, @Valid @RequestBody EdicionUsuarioDTO dto) throws Exception {
        UsuarioDTO updated = usuarioServicio.editar(new EdicionUsuarioDTO(id, dto.nombre(), dto.telefono(), dto.foto(), dto.rol()));
        return ResponseEntity.ok(new RespuestaDTO<>(false, updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RespuestaDTO<UsuarioDTO>> obtener(@PathVariable("id") Long id) throws Exception {
        return ResponseEntity.ok(new RespuestaDTO<>(false, usuarioServicio.obtener(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaDTO<String>> eliminar(@PathVariable("id") Long id) throws Exception {
        usuarioServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/contrasena")
    public ResponseEntity<RespuestaDTO<String>> cambiarContrasena(@PathVariable("id") Long id, @Valid @RequestBody CambioContrasenaDTO dto) throws Exception {
        usuarioServicio.cambiarContrasena(id, dto);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Contraseña cambiada correctamente"));
    }
    /// users/host
    /// users/{id}/places
    /// users/{id}/bookings


}