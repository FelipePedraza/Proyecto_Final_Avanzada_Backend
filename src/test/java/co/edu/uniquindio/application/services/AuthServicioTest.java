package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.usuario.LoginDTO;
import co.edu.uniquindio.application.dtos.usuario.TokenDTO;
import co.edu.uniquindio.application.exceptions.CuentaBloqueadaException;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.models.enums.Estado;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
        ReflectionTestUtils.setField(authServicio, "maxIntentos", 5);
        ReflectionTestUtils.setField(authServicio, "tiempoBloqueoMinutos", 15);
    }

    private Usuario crearUsuarioActivo(String email, String contrasena) {
        Usuario usuario = new Usuario();
        usuario.setId("123");
        usuario.setEmail(email);
        usuario.setContrasena(contrasena);
        usuario.setNombre("Juan Perez");
        usuario.setRol(Rol.Huesped);
        usuario.setEstado(Estado.ACTIVO);
        return usuario;
    }

    @Test
    void testLoginExitoso() throws Exception {
        var loginDTO = new LoginDTO("juan@email.com", "Password123");
        var usuario = crearUsuarioActivo(loginDTO.email(), "encodedPassword");
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
        verify(usuarioRepositorio, times(1)).save(usuario);
        assertEquals(0, usuario.getIntentosFallidos());
        assertNull(usuario.getBloqueadoHasta());
        assertEquals(1, meterRegistry.counter("auth.login.exitoso").count());
    }

    @Test
    void testLoginEmailNoExiste() {
        var loginDTO = new LoginDTO("noexiste@email.com", "Password123");

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
        var loginDTO = new LoginDTO("juan@email.com", "wrongPassword");
        var usuario = crearUsuarioActivo(loginDTO.email(), "encodedPassword");

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
        verify(usuarioRepositorio, times(1)).save(usuario);
        assertEquals(1, usuario.getIntentosFallidos());
        assertNull(usuario.getBloqueadoHasta());
        assertEquals(1, meterRegistry.counter("auth.login.fallido").count());
    }

    @Test
    void testLoginUsuarioBloqueado() {
        var loginDTO = new LoginDTO("juan@email.com", "Password123");
        var usuario = crearUsuarioActivo(loginDTO.email(), "encodedPassword");
        usuario.setIntentosFallidos(5);
        usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(10));

        when(usuarioRepositorio.findByEmail(loginDTO.email())).thenReturn(Optional.of(usuario));

        CuentaBloqueadaException exception = assertThrows(
                CuentaBloqueadaException.class,
                () -> authServicio.login(loginDTO)
        );

        assertEquals("Demasiados intentos fallidos. Intente de nuevo más tarde.", exception.getMessage());
        verify(usuarioRepositorio, times(1)).findByEmail(loginDTO.email());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtils, never()).generarToken(anyString(), anyMap());
        assertEquals(1, meterRegistry.counter("auth.login.bloqueado").count());
    }

    @Test
    void testLoginBloqueoExpirado() throws Exception {
        var loginDTO = new LoginDTO("juan@email.com", "Password123");
        var usuario = crearUsuarioActivo(loginDTO.email(), "encodedPassword");
        usuario.setIntentosFallidos(5);
        usuario.setBloqueadoHasta(LocalDateTime.now().minusMinutes(5));
        String tokenGenerado = "token-after-expiry";

        when(usuarioRepositorio.findByEmail(loginDTO.email())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(loginDTO.contrasena(), usuario.getContrasena())).thenReturn(true);
        when(jwtUtils.generarToken(anyString(), anyMap())).thenReturn(tokenGenerado);

        TokenDTO resultado = authServicio.login(loginDTO);

        assertNotNull(resultado);
        assertEquals(tokenGenerado, resultado.token());
        assertEquals(0, usuario.getIntentosFallidos());
        assertNull(usuario.getBloqueadoHasta());
        verify(usuarioRepositorio, times(1)).save(usuario);
        assertEquals(1, meterRegistry.counter("auth.login.exitoso").count());
    }

    @Test
    void testLoginBloqueoPorIntentos() {
        var loginDTO = new LoginDTO("juan@email.com", "wrongPassword");
        var usuario = crearUsuarioActivo(loginDTO.email(), "encodedPassword");
        usuario.setIntentosFallidos(4);

        when(usuarioRepositorio.findByEmail(loginDTO.email())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(loginDTO.contrasena(), usuario.getContrasena())).thenReturn(false);

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authServicio.login(loginDTO)
        );

        assertEquals("Credenciales inválidas", exception.getMessage());
        assertEquals(5, usuario.getIntentosFallidos());
        assertNotNull(usuario.getBloqueadoHasta());
        assertTrue(usuario.getBloqueadoHasta().isAfter(LocalDateTime.now()));
        verify(usuarioRepositorio, times(1)).save(usuario);
        assertEquals(1, meterRegistry.counter("auth.login.fallido").count());
    }

    @Test
    void testLoginExitosoReiniciaContadores() throws Exception {
        var loginDTO = new LoginDTO("juan@email.com", "Password123");
        var usuario = crearUsuarioActivo(loginDTO.email(), "encodedPassword");
        usuario.setIntentosFallidos(3);
        usuario.setBloqueadoHasta(null);
        String tokenGenerado = "token-reset";

        when(usuarioRepositorio.findByEmail(loginDTO.email())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(loginDTO.contrasena(), usuario.getContrasena())).thenReturn(true);
        when(jwtUtils.generarToken(anyString(), anyMap())).thenReturn(tokenGenerado);

        TokenDTO resultado = authServicio.login(loginDTO);

        assertNotNull(resultado);
        assertEquals(0, usuario.getIntentosFallidos());
        assertNull(usuario.getBloqueadoHasta());
        verify(usuarioRepositorio, times(1)).save(usuario);
        assertEquals(1, meterRegistry.counter("auth.login.exitoso").count());
    }

    @Test
    void testLoginUsuarioEliminado() {
        var loginDTO = new LoginDTO("eliminado@email.com", "Password123");
        var usuario = crearUsuarioActivo(loginDTO.email(), "encodedPassword");
        usuario.setEstado(Estado.ELIMINADO);

        when(usuarioRepositorio.findByEmail(loginDTO.email())).thenReturn(Optional.of(usuario));

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authServicio.login(loginDTO)
        );

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(usuarioRepositorio, times(1)).findByEmail(loginDTO.email());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        assertEquals(1, meterRegistry.counter("auth.login.fallido").count());
    }
}
