package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.usuario.LoginDTO;
import co.edu.uniquindio.application.dtos.usuario.TokenDTO;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.models.enums.Rol;
import co.edu.uniquindio.application.repositories.ContrasenaCodigoReinicioRepositorio;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.security.JWTUtils;
import co.edu.uniquindio.application.services.impl.AuthServicioImpl;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServicioTest {

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private JWTUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ContrasenaCodigoReinicioRepositorio contrasenaCodigoReinicioRepositorio;

    @Mock
    private EmailServicio emailServicio;

    private SimpleMeterRegistry meterRegistry;
    private AuthServicioImpl authServicio;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        authServicio = new AuthServicioImpl(
                usuarioRepositorio, jwtUtils, passwordEncoder,
                contrasenaCodigoReinicioRepositorio, emailServicio, meterRegistry);
    }

    @Test
    void testLoginExitoso() throws Exception {
        var loginDTO = new LoginDTO(
                "juan@email.com",
                "Password123"
        );

        var usuario = new Usuario();
        usuario.setId("123");
        usuario.setEmail(loginDTO.email());
        usuario.setContrasena("encodedPassword");
        usuario.setNombre("Juan Perez");
        usuario.setRol(Rol.Huesped);
        usuario.setEstado(co.edu.uniquindio.application.models.enums.Estado.ACTIVO);

        String tokenGenerado = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        when(usuarioRepositorio.findByEmail(loginDTO.email())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(loginDTO.contrasena(), usuario.getContrasena())).thenReturn(true);
        when(jwtUtils.generarToken(anyString(), anyMap())).thenReturn(tokenGenerado);

        TokenDTO resultado = authServicio.login(loginDTO);

        assertNotNull(resultado);
        assertEquals(tokenGenerado, resultado.token());
        verify(usuarioRepositorio, times(1)).findByEmail(loginDTO.email());
        verify(passwordEncoder, times(1)).matches(loginDTO.contrasena(), usuario.getContrasena());
        verify(jwtUtils, times(1)).generarToken(anyString(), anyMap());
        assertEquals(1, meterRegistry.counter("auth.login.exitoso").count());
    }

    @Test
    void testLoginEmailNoExiste() {
        var loginDTO = new LoginDTO(
                "noexiste@email.com",
                "Password123"
        );

        when(usuarioRepositorio.findByEmail(loginDTO.email())).thenReturn(Optional.empty());

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authServicio.login(loginDTO)
        );

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(usuarioRepositorio, times(1)).findByEmail(loginDTO.email());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtils, never()).generarToken(anyString(), anyMap());
        assertEquals(1, meterRegistry.counter("auth.login.fallido").count());
    }

    @Test
    void testLoginContrasenaIncorrecta() {
        var loginDTO = new LoginDTO(
                "juan@email.com",
                "wrongPassword"
        );

        var usuario = new Usuario();
        usuario.setId("123");
        usuario.setEmail(loginDTO.email());
        usuario.setContrasena("encodedPassword");
        usuario.setEstado(co.edu.uniquindio.application.models.enums.Estado.ACTIVO);

        when(usuarioRepositorio.findByEmail(loginDTO.email())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(loginDTO.contrasena(), usuario.getContrasena())).thenReturn(false);

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authServicio.login(loginDTO)
        );

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(usuarioRepositorio, times(1)).findByEmail(loginDTO.email());
        verify(passwordEncoder, times(1)).matches(loginDTO.contrasena(), usuario.getContrasena());
        verify(jwtUtils, never()).generarToken(anyString(), anyMap());
        assertEquals(1, meterRegistry.counter("auth.login.fallido").count());
    }
}
