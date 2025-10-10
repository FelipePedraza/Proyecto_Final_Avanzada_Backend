package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.usuario.LoginDTO;
import co.edu.uniquindio.application.dtos.usuario.TokenDTO;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.models.enums.Rol;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.security.JWTUtils;
import co.edu.uniquindio.application.services.impl.AuthServicioImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private AuthServicioImpl authServicio;

    /**
     * Prueba login exitoso
     */
    @Test
    void testLoginExitoso() throws Exception {
        // Sección de Arrange: Se definen los datos de login
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

        String tokenGenerado = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        when(usuarioRepositorio.findByEmail(loginDTO.email())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(loginDTO.contrasena(), usuario.getContrasena())).thenReturn(true);
        when(jwtUtils.generarToken(anyString(), anyMap())).thenReturn(tokenGenerado);

        // Sección de Act: Ejecutar la acción de login
        TokenDTO resultado = authServicio.login(loginDTO);

        // Sección de Assert: Verificar que se obtuvo el token
        assertNotNull(resultado);
        assertEquals(tokenGenerado, resultado.token());
        verify(usuarioRepositorio, times(1)).findByEmail(loginDTO.email());
        verify(passwordEncoder, times(1)).matches(loginDTO.contrasena(), usuario.getContrasena());
        verify(jwtUtils, times(1)).generarToken(anyString(), anyMap());
    }

    /**
     * Prueba login con email que no existe
     */
    @Test
    void testLoginEmailNoExiste() {
        // Sección de Arrange: Se definen los datos con email inexistente
        var loginDTO = new LoginDTO(
                "noexiste@email.com",
                "Password123"
        );

        when(usuarioRepositorio.findByEmail(loginDTO.email())).thenReturn(Optional.empty());

        // Sección de Act & Assert: Verificar que se lanza la excepción
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authServicio.login(loginDTO)
        );

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(usuarioRepositorio, times(1)).findByEmail(loginDTO.email());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtils, never()).generarToken(anyString(), anyMap());
    }

    /**
     * Prueba login con contraseña incorrecta
     */
    @Test
    void testLoginContrasenaIncorrecta() {
        // Sección de Arrange: Se definen los datos con contraseña incorrecta
        var loginDTO = new LoginDTO(
                "juan@email.com",
                "wrongPassword"
        );

        var usuario = new Usuario();
        usuario.setId("123");
        usuario.setEmail(loginDTO.email());
        usuario.setContrasena("encodedPassword");

        when(usuarioRepositorio.findByEmail(loginDTO.email())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(loginDTO.contrasena(), usuario.getContrasena())).thenReturn(false);

        // Sección de Act & Assert: Verificar que se lanza la excepción
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authServicio.login(loginDTO)
        );

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(usuarioRepositorio, times(1)).findByEmail(loginDTO.email());
        verify(passwordEncoder, times(1)).matches(loginDTO.contrasena(), usuario.getContrasena());
        verify(jwtUtils, never()).generarToken(anyString(), anyMap());
    }
}

