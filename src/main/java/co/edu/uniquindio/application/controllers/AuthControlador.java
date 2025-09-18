package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.usuario.*;
import co.edu.uniquindio.application.services.AuthServicio;
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

    @PostMapping("/registro")
    public ResponseEntity<RespuestaDTO<UsuarioDTO>> registrar(@Valid @RequestBody CreacionAnfitrionDTO anfitrionDTO) throws Exception {
        UsuarioDTO usuario = authServicio.registro(anfitrionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RespuestaDTO<>(false, usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<RespuestaDTO<TokenDTO>> login(@Valid @RequestBody LoginDTO loginDTO) throws Exception {
        TokenDTO loginResponse = authServicio.login(loginDTO);
        return ResponseEntity.ok(new RespuestaDTO<>(false, loginResponse));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<RespuestaDTO<String>> solicitarRecuperacion(@Valid @RequestBody OlvidoContrasenaDTO olvidoContrasenaDTO) throws Exception {
        authServicio.solicitarRecuperacion(olvidoContrasenaDTO);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Código de recuperación enviado al correo."));
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<RespuestaDTO<String>> restablecerContrasena(@Valid @RequestBody ReiniciarContrasena reiniciarContrasena) throws Exception {
        authServicio.restablecerContrasena(reiniciarContrasena);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Contraseña restablecida correctamente."));
    }
}
