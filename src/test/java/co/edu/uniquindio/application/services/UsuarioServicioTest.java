package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.EmailDTO;
import co.edu.uniquindio.application.dtos.usuario.*;
import co.edu.uniquindio.application.exceptions.NoFoundException;
import co.edu.uniquindio.application.exceptions.ValidationException;
import co.edu.uniquindio.application.exceptions.ValueConflictException;
import co.edu.uniquindio.application.mappers.AlojamientoMapper;
import co.edu.uniquindio.application.mappers.PerfilAnfitrionMapper;
import co.edu.uniquindio.application.mappers.ReservaMapper;
import co.edu.uniquindio.application.mappers.UsuarioMapper;
import co.edu.uniquindio.application.models.entitys.PerfilAnfitrion;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.models.enums.Estado;
import co.edu.uniquindio.application.models.enums.Rol;
import co.edu.uniquindio.application.repositories.*;
import co.edu.uniquindio.application.services.impl.UsuarioServicioImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServicioTest {

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private ContrasenaCodigoReinicioRepositorio contrasenaCodigoReinicioRepositorio;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthServicio authServicio;

    @Mock
    private EmailServicio emailServicio;

    @Mock
    private ImagenServicio imagenServicio;

    @Mock
    private PerfilAnfitrionRepositorio perfilAnfitrionRepositorio;

    @Mock
    private PerfilAnfitrionMapper perfilAnfitrionMapper;

    @Mock
    private AlojamientoRepositorio alojamientoRepositorio;

    @Mock
    private AlojamientoMapper alojamientoMapper;

    @Mock
    private ReservaRepositorio reservaRepositorio;

    @Mock
    private ReservaMapper reservaMapper;

    @InjectMocks
    private UsuarioServicioImpl usuarioServicio;

    /**
     * Prueba crear usuario exitosamente
     */
    @Test
    void testCrearUsuarioExitoso() throws Exception {
        // Sección de Arrange: Se definen los datos del usuario a crear
        var usuarioDTO = new CreacionUsuarioDTO(
                "Juan Perez",
                "juan@email.com",
                "Password123",
                "123456789",
                LocalDate.of(1990, 1, 1)
        );

        var nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail(usuarioDTO.email());
        nuevoUsuario.setNombre(usuarioDTO.nombre());

        // Configurar el comportamiento del mock
        when(usuarioRepositorio.findByEmail(usuarioDTO.email())).thenReturn(Optional.empty());
        when(usuarioMapper.toEntity(usuarioDTO)).thenReturn(nuevoUsuario);
        when(passwordEncoder.encode(usuarioDTO.contrasena())).thenReturn("encodedPassword");
        when(usuarioRepositorio.save(any(Usuario.class))).thenReturn(nuevoUsuario);
        doNothing().when(emailServicio).enviarEmail(any(EmailDTO.class));

        // Sección de Act: Ejecutar la acción de creación del usuario
        usuarioServicio.crear(usuarioDTO);

        // Sección de Assert: Verificar que se llamaron los métodos esperados
        verify(usuarioRepositorio, times(1)).findByEmail(usuarioDTO.email());
        verify(usuarioMapper, times(1)).toEntity(usuarioDTO);
        verify(passwordEncoder, times(1)).encode(usuarioDTO.contrasena());
        verify(usuarioRepositorio, times(1)).save(any(Usuario.class));
        verify(emailServicio, times(1)).enviarEmail(any(EmailDTO.class));
    }

    /**
     * Prueba crear usuario cuando el email ya existe
     */
    @Test
    void testCrearUsuarioEmailExistente() {
        // Sección de Arrange: Se definen los datos del usuario con email existente
        var usuarioDTO = new CreacionUsuarioDTO(
                "Juan Perez",
                "juan@email.com",
                "Password123",
                "123456789",
                LocalDate.of(1990, 1, 1)
        );

        var usuarioExistente = new Usuario();
        usuarioExistente.setEmail(usuarioDTO.email());

        when(usuarioRepositorio.findByEmail(usuarioDTO.email())).thenReturn(Optional.of(usuarioExistente));

        // Sección de Act & Assert: Verificar que se lanza la excepción
        ValueConflictException exception = assertThrows(
                ValueConflictException.class,
                () -> usuarioServicio.crear(usuarioDTO)
        );

        assertEquals("El email ya existe", exception.getMessage());
        verify(usuarioRepositorio, times(1)).findByEmail(usuarioDTO.email());
        verify(usuarioRepositorio, never()).save(any(Usuario.class));
    }

    /**
     * Prueba obtener usuario exitosamente
     */
    @Test
    void testObtenerUsuarioExitoso() throws Exception {
        // Sección de Arrange: Se define el usuario a obtener
        String id = "123";
        var usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre("Juan Perez");
        usuario.setEmail("juan@email.com");

        var usuarioDTO = new UsuarioDTO(
                id,
                "Juan Perez",
                "juan@email.com",
                "123456789",
                Rol.Huesped,
                LocalDate.of(1990, 1, 1),
                "http://photo.url"
        );

        when(usuarioRepositorio.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toUserDTO(usuario)).thenReturn(usuarioDTO);

        // Sección de Act: Ejecutar la acción de obtener el usuario
        UsuarioDTO resultado = usuarioServicio.obtener(id);

        // Sección de Assert: Verificar que se obtuvieron los datos correctos
        assertNotNull(resultado);
        assertEquals(id, resultado.id());
        assertEquals("Juan Perez", resultado.nombre());
        assertEquals("juan@email.com", resultado.email());
        verify(usuarioRepositorio, times(1)).findById(id);
        verify(usuarioMapper, times(1)).toUserDTO(usuario);
    }

    /**
     * Prueba obtener usuario que no existe
     */
    @Test
    void testObtenerUsuarioNoExiste() {
        // Sección de Arrange: Se define un id que no existe
        String id = "999";
        when(usuarioRepositorio.findById(id)).thenReturn(Optional.empty());

        // Sección de Act & Assert: Verificar que se lanza la excepción
        NoFoundException exception = assertThrows(
                NoFoundException.class,
                () -> usuarioServicio.obtener(id)
        );

        assertEquals("No se encontro el usuario con el id: " + id, exception.getMessage());
        verify(usuarioRepositorio, times(1)).findById(id);
    }

    /**
     * Prueba eliminar usuario exitosamente
     */
    @Test
    void testEliminarUsuarioExitoso() throws Exception {
        // Sección de Arrange: Se define el usuario a eliminar
        String id = "123";
        var usuario = new Usuario();
        usuario.setId(id);
        usuario.setEstado(Estado.ACTIVO);

        when(usuarioRepositorio.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepositorio.save(any(Usuario.class))).thenReturn(usuario);

        // Sección de Act: Ejecutar la acción de eliminar el usuario
        usuarioServicio.eliminar(id);

        // Sección de Assert: Verificar que se cambió el estado a ELIMINADO
        verify(usuarioRepositorio, times(1)).findById(id);
        verify(usuarioRepositorio, times(1)).save(usuario);
        assertEquals(Estado.ELIMINADO, usuario.getEstado());
    }

    /**
     * Prueba cambiar contraseña exitosamente
     */
    @Test
    void testCambiarContrasenaExitoso() throws Exception {
        // Sección de Arrange: Se definen los datos para cambiar la contraseña
        String id = "123";
        var cambioContrasenaDTO = new CambioContrasenaDTO(
                "oldPassword",
                "newPassword"
        );

        var usuario = new Usuario();
        usuario.setId(id);
        usuario.setContrasena("encodedOldPassword");

        when(usuarioRepositorio.findById(id)).thenReturn(Optional.of(usuario));
        when(authServicio.obtnerIdAutenticado(id)).thenReturn(true);
        when(passwordEncoder.matches(cambioContrasenaDTO.contrasenaActual(), usuario.getContrasena())).thenReturn(true);
        when(passwordEncoder.encode(cambioContrasenaDTO.contrasenaNueva())).thenReturn("encodedNewPassword");
        when(usuarioRepositorio.save(any(Usuario.class))).thenReturn(usuario);

        // Sección de Act: Ejecutar la acción de cambiar contraseña
        usuarioServicio.cambiarContrasena(id, cambioContrasenaDTO);

        // Sección de Assert: Verificar que se cambió la contraseña
        verify(usuarioRepositorio, times(1)).findById(id);
        verify(authServicio, times(1)).obtnerIdAutenticado(id);
        verify(passwordEncoder, times(1)).matches(cambioContrasenaDTO.contrasenaActual(), "encodedOldPassword");
        verify(passwordEncoder, times(1)).encode(cambioContrasenaDTO.contrasenaNueva());
        verify(usuarioRepositorio, times(1)).save(usuario);
    }

    /**
     * Prueba cambiar contraseña con contraseña actual incorrecta
     */
    @Test
    void testCambiarContrasenaActualIncorrecta() {
        // Sección de Arrange: Se definen los datos con contraseña actual incorrecta
        String id = "123";
        var cambioContrasenaDTO = new CambioContrasenaDTO(
                "wrongPassword",
                "newPassword"
        );

        var usuario = new Usuario();
        usuario.setId(id);
        usuario.setContrasena("encodedOldPassword");

        when(usuarioRepositorio.findById(id)).thenReturn(Optional.of(usuario));
        when(authServicio.obtnerIdAutenticado(id)).thenReturn(true);
        when(passwordEncoder.matches(cambioContrasenaDTO.contrasenaActual(), usuario.getContrasena())).thenReturn(false);

        // Sección de Act & Assert: Verificar que se lanza la excepción
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> usuarioServicio.cambiarContrasena(id, cambioContrasenaDTO)
        );

        assertEquals("La contraseña actual es incorrecta.", exception.getMessage());
        verify(usuarioRepositorio, never()).save(any(Usuario.class));
    }

    /**
     * Prueba cambiar contraseña cuando la nueva es igual a la actual
     */
    @Test
    void testCambiarContrasenaNuevaIgualActual() {
        // Sección de Arrange: Se definen los datos con contraseña nueva igual a la actual
        String id = "123";
        var cambioContrasenaDTO = new CambioContrasenaDTO(
                "samePassword",
                "samePassword"
        );

        var usuario = new Usuario();
        usuario.setId(id);
        usuario.setContrasena("encodedPassword");

        when(usuarioRepositorio.findById(id)).thenReturn(Optional.of(usuario));
        when(authServicio.obtnerIdAutenticado(id)).thenReturn(true);
        when(passwordEncoder.matches(cambioContrasenaDTO.contrasenaActual(), usuario.getContrasena())).thenReturn(true);

        // Sección de Act & Assert: Verificar que se lanza la excepción
        ValueConflictException exception = assertThrows(
                ValueConflictException.class,
                () -> usuarioServicio.cambiarContrasena(id, cambioContrasenaDTO)
        );

        assertEquals("La nueva contraseña no puede ser igual a la actual.", exception.getMessage());
        verify(usuarioRepositorio, never()).save(any(Usuario.class));
    }

    /**
     * Prueba cambiar contraseña sin permisos
     */
    @Test
    void testCambiarContrasenaSinPermisos() {
        // Sección de Arrange: Se definen los datos sin permisos
        String id = "123";
        var cambioContrasenaDTO = new CambioContrasenaDTO(
                "oldPassword",
                "newPassword"
        );

        var usuario = new Usuario();
        usuario.setId(id);

        when(usuarioRepositorio.findById(id)).thenReturn(Optional.of(usuario));
        when(authServicio.obtnerIdAutenticado(id)).thenReturn(false);

        // Sección de Act & Assert: Verificar que se lanza la excepción
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> usuarioServicio.cambiarContrasena(id, cambioContrasenaDTO)
        );

        assertEquals("No tiene permisos para cambiar la contraseña de este usuario.", exception.getMessage());
        verify(usuarioRepositorio, never()).save(any(Usuario.class));
    }

    /**
     * Prueba crear anfitrión exitosamente
     */
    @Test
    void testCrearAnfitrionExitoso() throws Exception {
        // Sección de Arrange: Se definen los datos para crear anfitrión
        String usuarioId = "123";
        var creacionAnfitrionDTO = new CreacionAnfitrionDTO(
                usuarioId,
                "Descripción del anfitrión",
                "http://portada.url"
        );

        var usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setEsAnfitrion(false);
        usuario.setEmail("juan@email.com");

        var perfilAnfitrion = new PerfilAnfitrion();

        when(authServicio.obtnerIdAutenticado(usuarioId)).thenReturn(true);
        when(usuarioRepositorio.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(perfilAnfitrionMapper.toEntity(creacionAnfitrionDTO)).thenReturn(perfilAnfitrion);
        when(perfilAnfitrionRepositorio.save(any(PerfilAnfitrion.class))).thenReturn(perfilAnfitrion);
        when(usuarioRepositorio.save(any(Usuario.class))).thenReturn(usuario);
        doNothing().when(emailServicio).enviarEmail(any(EmailDTO.class));

        // Sección de Act: Ejecutar la acción de crear anfitrión
        usuarioServicio.crearAnfitrion(creacionAnfitrionDTO);

        // Sección de Assert: Verificar que se creó el perfil de anfitrión
        verify(authServicio, times(1)).obtnerIdAutenticado(usuarioId);
        verify(usuarioRepositorio, times(1)).findById(usuarioId);
        verify(perfilAnfitrionMapper, times(1)).toEntity(creacionAnfitrionDTO);
        verify(perfilAnfitrionRepositorio, times(1)).save(any(PerfilAnfitrion.class));
        verify(usuarioRepositorio, times(1)).save(usuario);
        verify(emailServicio, times(1)).enviarEmail(any(EmailDTO.class));
        assertEquals(Rol.Anfitrion, usuario.getRol());
        assertTrue(usuario.getEsAnfitrion());
    }

    /**
     * Prueba crear anfitrión cuando el usuario ya es anfitrión
     */
    @Test
    void testCrearAnfitrionYaEsAnfitrion() {
        // Sección de Arrange: Se definen los datos con usuario ya anfitrión
        String usuarioId = "123";
        var creacionAnfitrionDTO = new CreacionAnfitrionDTO(
                usuarioId,
                "Descripción del anfitrión",
                "http://portada.url"
        );

        var usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setEsAnfitrion(true);

        when(authServicio.obtnerIdAutenticado(usuarioId)).thenReturn(true);
        when(usuarioRepositorio.findById(usuarioId)).thenReturn(Optional.of(usuario));

        // Sección de Act & Assert: Verificar que se lanza la excepción
        ValueConflictException exception = assertThrows(
                ValueConflictException.class,
                () -> usuarioServicio.crearAnfitrion(creacionAnfitrionDTO)
        );

        assertEquals("El usuario ya es un anfitrion", exception.getMessage());
        verify(perfilAnfitrionRepositorio, never()).save(any(PerfilAnfitrion.class));
    }

    /**
     * Prueba crear anfitrión sin permisos
     */
    @Test
    void testCrearAnfitrionSinPermisos() {
        // Sección de Arrange: Se definen los datos sin permisos
        String usuarioId = "123";
        var creacionAnfitrionDTO = new CreacionAnfitrionDTO(
                usuarioId,
                "Descripción del anfitrión",
                "http://portada.url"
        );

        when(authServicio.obtnerIdAutenticado(usuarioId)).thenReturn(false);

        // Sección de Act & Assert: Verificar que se lanza la excepción
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> usuarioServicio.crearAnfitrion(creacionAnfitrionDTO)
        );

        assertEquals("No tiene permisos para crear perfil de anfitrión para este usuario.", exception.getMessage());
        verify(usuarioRepositorio, never()).findById(anyString());
    }

    /**
     * Prueba verificar que existe un usuario por email
     */
    @Test
    void testExistePorEmail() {
        // Sección de Arrange: Se define un email existente
        String email = "juan@email.com";
        var usuario = new Usuario();
        usuario.setEmail(email);

        when(usuarioRepositorio.findByEmail(email)).thenReturn(Optional.of(usuario));

        // Sección de Act: Ejecutar la verificación
        boolean existe = usuarioServicio.existePorEmail(email);

        // Sección de Assert: Verificar que el resultado es true
        assertTrue(existe);
        verify(usuarioRepositorio, times(1)).findByEmail(email);
    }

    /**
     * Prueba verificar que no existe un usuario por email
     */
    @Test
    void testNoExistePorEmail() {
        // Sección de Arrange: Se define un email que no existe
        String email = "noexiste@email.com";

        when(usuarioRepositorio.findByEmail(email)).thenReturn(Optional.empty());

        // Sección de Act: Ejecutar la verificación
        boolean existe = usuarioServicio.existePorEmail(email);

        // Sección de Assert: Verificar que el resultado es false
        assertFalse(existe);
        verify(usuarioRepositorio, times(1)).findByEmail(email);
    }
}

