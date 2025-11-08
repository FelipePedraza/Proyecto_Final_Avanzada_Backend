package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.usuario.*;
import co.edu.uniquindio.application.services.AuthServicio;
import co.edu.uniquindio.application.services.UsuarioServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthControlador {

    private final AuthServicio authServicio;
    private final UsuarioServicio usuarioServicio;

    @PostMapping("/registro")
    public ResponseEntity<RespuestaDTO<String>> crear(@Valid @RequestBody CreacionUsuarioDTO usuarioDTO) throws Exception {
        usuarioServicio.crear(usuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RespuestaDTO<>(false, "El registro ha sido exitoso"));
    }

    @PostMapping("/login")
    public ResponseEntity<RespuestaDTO<TokenDTO>> login(@Valid @RequestBody LoginDTO loginDTO) throws Exception {
        TokenDTO token = authServicio.login(loginDTO);
        return ResponseEntity.ok(new RespuestaDTO<>(false, token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<RespuestaDTO<String>> solicitarRecuperacion(@Valid @RequestBody OlvidoContrasenaDTO olvidoContrasenaDTO) throws Exception {
        authServicio.solicitarRecuperacion(olvidoContrasenaDTO);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Código de recuperación enviado al correo."));
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<RespuestaDTO<String>> restablecerContrasena(@Valid @RequestBody ReinicioContrasenaDTO reinicioContrasenaDTO) throws Exception {
        authServicio.reiniciarContrasena(reinicioContrasenaDTO);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Contraseña restablecida correctamente."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RespuestaDTO<TokenDTO>> refresh(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) throws Exception {
        TokenDTO token = authServicio.refrescarToken(refreshTokenDTO);
        return ResponseEntity.ok(new RespuestaDTO<>(false, token));
    }
}
