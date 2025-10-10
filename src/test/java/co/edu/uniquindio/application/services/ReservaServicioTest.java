package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.EmailDTO;
import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.exceptions.NoFoundException;
import co.edu.uniquindio.application.exceptions.ValidationException;
import co.edu.uniquindio.application.mappers.ReservaMapper;
import co.edu.uniquindio.application.models.entitys.Alojamiento;
import co.edu.uniquindio.application.models.entitys.Reserva;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.models.enums.Estado;
import co.edu.uniquindio.application.models.enums.ReservaEstado;
import co.edu.uniquindio.application.repositories.AlojamientoRepositorio;
import co.edu.uniquindio.application.repositories.ReservaRepositorio;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.services.impl.ReservaServicioImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservaServicioTest {

    @Mock
    private ReservaRepositorio reservaRepositorio;

    @Mock
    private AlojamientoRepositorio alojamientoRepositorio;

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private ReservaMapper reservaMapper;

    @Mock
    private EmailServicio emailServicio;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReservaServicioImpl reservaServicio;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Prueba crear reserva exitosamente
     */
    @Test
    void testCrearReservaExitoso() throws Exception {
        // Sección de Arrange: Se definen los datos de la reserva
        String usuarioId = "123";
        Long alojamientoId = 1L;
        LocalDate fechaEntrada = LocalDate.now().plusDays(5);
        LocalDate fechaSalida = LocalDate.now().plusDays(10);

        var creacionReservaDTO = new CreacionReservaDTO(
                alojamientoId,
                usuarioId,
                fechaEntrada,
                fechaSalida,
                2
        );

        var huesped = new Usuario();
        huesped.setId(usuarioId);
        huesped.setEstado(Estado.ACTIVO);
        huesped.setNombre("Juan Perez");
        huesped.setEmail("juan@email.com");

        var anfitrion = new Usuario();
        anfitrion.setId("456");
        anfitrion.setNombre("Pedro Gomez");
        anfitrion.setEmail("pedro@email.com");

        var alojamiento = new Alojamiento();
        alojamiento.setId(alojamientoId);
        alojamiento.setEstado(Estado.ACTIVO);
        alojamiento.setMaxHuespedes(4);
        alojamiento.setPrecioPorNoche(100.0f);
        alojamiento.setAnfitrion(anfitrion);
        alojamiento.setTitulo("Casa en la playa");

        var reserva = new Reserva();
        reserva.setId(1L);
        reserva.setFechaEntrada(fechaEntrada);
        reserva.setFechaSalida(fechaSalida);
        reserva.setCantidadHuespedes(2);

        User userDetails = new User(usuarioId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(usuarioRepositorio.findById(usuarioId)).thenReturn(Optional.of(huesped));
        when(alojamientoRepositorio.findById(alojamientoId)).thenReturn(Optional.of(alojamiento));
        when(reservaMapper.toEntity(creacionReservaDTO)).thenReturn(reserva);
        when(reservaRepositorio.findByAlojamiento_IdAndEstadoIn(anyLong(), any())).thenReturn(new ArrayList<>());
        when(reservaRepositorio.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(emailServicio).enviarEmail(any(EmailDTO.class));

        // Sección de Act: Ejecutar la acción de crear reserva
        reservaServicio.crear(creacionReservaDTO);

        // Sección de Assert: Verificar que se creó la reserva
        verify(usuarioRepositorio, times(1)).findById(usuarioId);
        verify(alojamientoRepositorio, times(1)).findById(alojamientoId);
        verify(reservaMapper, times(1)).toEntity(creacionReservaDTO);
        verify(reservaRepositorio, times(1)).save(any(Reserva.class));
        verify(emailServicio, times(2)).enviarEmail(any(EmailDTO.class));
    }

    /**
     * Prueba crear reserva cuando el usuario no existe
     */
    @Test
    void testCrearReservaUsuarioNoExiste() {
        // Sección de Arrange: Se definen los datos con usuario inexistente
        String usuarioId = "999";
        var creacionReservaDTO = new CreacionReservaDTO(
                1L,
                usuarioId,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                2
        );

        User userDetails = new User(usuarioId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(usuarioRepositorio.findById(usuarioId)).thenReturn(Optional.empty());

        // Sección de Act & Assert: Verificar que se lanza la excepción
        NoFoundException exception = assertThrows(
                NoFoundException.class,
                () -> reservaServicio.crear(creacionReservaDTO)
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(usuarioRepositorio, times(1)).findById(usuarioId);
        verify(reservaRepositorio, never()).save(any(Reserva.class));
    }

    /**
     * Prueba crear reserva cuando el alojamiento no existe
     */
    @Test
    void testCrearReservaAlojamientoNoExiste() {
        // Sección de Arrange: Se definen los datos con alojamiento inexistente
        String usuarioId = "123";
        Long alojamientoId = 999L;

        var creacionReservaDTO = new CreacionReservaDTO(
                alojamientoId,
                usuarioId,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                2
        );

        var huesped = new Usuario();
        huesped.setId(usuarioId);
        huesped.setEstado(Estado.ACTIVO);

        User userDetails = new User(usuarioId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(usuarioRepositorio.findById(usuarioId)).thenReturn(Optional.of(huesped));
        when(alojamientoRepositorio.findById(alojamientoId)).thenReturn(Optional.empty());

        // Sección de Act & Assert: Verificar que se lanza la excepción
        NoFoundException exception = assertThrows(
                NoFoundException.class,
                () -> reservaServicio.crear(creacionReservaDTO)
        );

        assertEquals("Alojamiento no encontrado", exception.getMessage());
        verify(alojamientoRepositorio, times(1)).findById(alojamientoId);
        verify(reservaRepositorio, never()).save(any(Reserva.class));
    }

    /**
     * Prueba crear reserva con fechas pasadas
     */
    @Test
    void testCrearReservaFechasPasadas() {
        // Sección de Arrange: Se definen los datos con fechas pasadas
        String usuarioId = "123";
        Long alojamientoId = 1L;

        var creacionReservaDTO = new CreacionReservaDTO(
                alojamientoId,
                usuarioId,
                LocalDate.now().minusDays(5),
                LocalDate.now().minusDays(1),
                2
        );

        var huesped = new Usuario();
        huesped.setId(usuarioId);
        huesped.setEstado(Estado.ACTIVO);

        var anfitrion = new Usuario();
        anfitrion.setId("456");

        var alojamiento = new Alojamiento();
        alojamiento.setId(alojamientoId);
        alojamiento.setEstado(Estado.ACTIVO);
        alojamiento.setAnfitrion(anfitrion);

        User userDetails = new User(usuarioId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(usuarioRepositorio.findById(usuarioId)).thenReturn(Optional.of(huesped));
        when(alojamientoRepositorio.findById(alojamientoId)).thenReturn(Optional.of(alojamiento));

        // Sección de Act & Assert: Verificar que se lanza la excepción
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> reservaServicio.crear(creacionReservaDTO)
        );

        assertEquals("No se pueden reservar fechas pasadas", exception.getMessage());
        verify(reservaRepositorio, never()).save(any(Reserva.class));
    }

    /**
     * Prueba crear reserva cuando se excede la capacidad
     */
    @Test
    void testCrearReservaExcedeCapacidad() {
        // Sección de Arrange: Se definen los datos excediendo la capacidad
        String usuarioId = "123";
        Long alojamientoId = 1L;

        var creacionReservaDTO = new CreacionReservaDTO(
                alojamientoId,
                usuarioId,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                10
        );

        var huesped = new Usuario();
        huesped.setId(usuarioId);
        huesped.setEstado(Estado.ACTIVO);

        var anfitrion = new Usuario();
        anfitrion.setId("456");

        var alojamiento = new Alojamiento();
        alojamiento.setId(alojamientoId);
        alojamiento.setEstado(Estado.ACTIVO);
        alojamiento.setMaxHuespedes(4);
        alojamiento.setAnfitrion(anfitrion);

        User userDetails = new User(usuarioId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(usuarioRepositorio.findById(usuarioId)).thenReturn(Optional.of(huesped));
        when(alojamientoRepositorio.findById(alojamientoId)).thenReturn(Optional.of(alojamiento));

        // Sección de Act & Assert: Verificar que se lanza la excepción
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> reservaServicio.crear(creacionReservaDTO)
        );

        assertTrue(exception.getMessage().contains("supera la capacidad máxima"));
        verify(reservaRepositorio, never()).save(any(Reserva.class));
    }

    /**
     * Prueba crear reserva cuando el usuario intenta reservar su propio alojamiento
     */
    @Test
    void testCrearReservaPropioAlojamiento() {
        // Sección de Arrange: Se definen los datos donde el usuario es el anfitrión
        String usuarioId = "123";
        Long alojamientoId = 1L;

        var creacionReservaDTO = new CreacionReservaDTO(
                alojamientoId,
                usuarioId,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                2
        );

        var huesped = new Usuario();
        huesped.setId(usuarioId);
        huesped.setEstado(Estado.ACTIVO);

        var alojamiento = new Alojamiento();
        alojamiento.setId(alojamientoId);
        alojamiento.setEstado(Estado.ACTIVO);
        alojamiento.setAnfitrion(huesped); // El mismo usuario es el anfitrión

        User userDetails = new User(usuarioId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(usuarioRepositorio.findById(usuarioId)).thenReturn(Optional.of(huesped));
        when(alojamientoRepositorio.findById(alojamientoId)).thenReturn(Optional.of(alojamiento));

        // Sección de Act & Assert: Verificar que se lanza la excepción
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> reservaServicio.crear(creacionReservaDTO)
        );

        assertEquals("No puedes reservar tu propio alojamiento", exception.getMessage());
        verify(reservaRepositorio, never()).save(any(Reserva.class));
    }

    /**
     * Prueba aceptar reserva exitosamente
     */
    @Test
    void testAceptarReservaExitoso() throws Exception {
        // Sección de Arrange: Se definen los datos de la reserva a aceptar
        Long reservaId = 1L;
        String anfitrionId = "456";

        var huesped = new Usuario();
        huesped.setId("123");
        huesped.setNombre("Juan Perez");
        huesped.setEmail("juan@email.com");

        var anfitrion = new Usuario();
        anfitrion.setId(anfitrionId);
        anfitrion.setNombre("Pedro Gomez");
        anfitrion.setEmail("pedro@email.com");

        var alojamiento = new Alojamiento();
        alojamiento.setId(1L);
        alojamiento.setAnfitrion(anfitrion);
        alojamiento.setTitulo("Casa en la playa");

        var reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setEstado(ReservaEstado.PENDIENTE);
        reserva.setAlojamiento(alojamiento);
        reserva.setHuesped(huesped);
        reserva.setFechaEntrada(LocalDate.now().plusDays(5));
        reserva.setFechaSalida(LocalDate.now().plusDays(10));
        reserva.setCantidadHuespedes(2);
        reserva.setPrecio(500.0);

        User userDetails = new User(anfitrionId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reservaRepositorio.findById(reservaId)).thenReturn(Optional.of(reserva));
        when(reservaRepositorio.findByAlojamiento_IdAndEstadoIn(anyLong(), any())).thenReturn(new ArrayList<>());
        when(reservaRepositorio.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(emailServicio).enviarEmail(any(EmailDTO.class));

        // Sección de Act: Ejecutar la acción de aceptar reserva
        reservaServicio.aceptarReserva(reservaId);

        // Sección de Assert: Verificar que se aceptó la reserva
        verify(reservaRepositorio, times(1)).findById(reservaId);
        verify(reservaRepositorio, times(1)).save(reserva);
        assertEquals(ReservaEstado.CONFIRMADA, reserva.getEstado());
        verify(emailServicio, times(2)).enviarEmail(any(EmailDTO.class));
    }

    /**
     * Prueba aceptar reserva cuando no se tiene permiso
     */
    @Test
    void testAceptarReservaSinPermiso() {
        // Sección de Arrange: Se definen los datos sin permiso
        Long reservaId = 1L;
        String usuarioId = "123";

        var anfitrion = new Usuario();
        anfitrion.setId("456");

        var alojamiento = new Alojamiento();
        alojamiento.setId(1L);
        alojamiento.setAnfitrion(anfitrion);

        var reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setEstado(ReservaEstado.PENDIENTE);
        reserva.setAlojamiento(alojamiento);

        User userDetails = new User(usuarioId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reservaRepositorio.findById(reservaId)).thenReturn(Optional.of(reserva));

        // Sección de Act & Assert: Verificar que se lanza la excepción
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> reservaServicio.aceptarReserva(reservaId)
        );

        assertEquals("Solo el anfitrión puede aceptar esta reserva", exception.getMessage());
        verify(reservaRepositorio, never()).save(any(Reserva.class));
    }

    /**
     * Prueba aceptar reserva que no está pendiente
     */
    @Test
    void testAceptarReservaNoPendiente() {
        // Sección de Arrange: Se definen los datos con reserva ya confirmada
        Long reservaId = 1L;
        String anfitrionId = "456";

        var anfitrion = new Usuario();
        anfitrion.setId(anfitrionId);

        var alojamiento = new Alojamiento();
        alojamiento.setId(1L);
        alojamiento.setAnfitrion(anfitrion);

        var reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setEstado(ReservaEstado.CONFIRMADA);
        reserva.setAlojamiento(alojamiento);

        User userDetails = new User(anfitrionId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reservaRepositorio.findById(reservaId)).thenReturn(Optional.of(reserva));

        // Sección de Act & Assert: Verificar que se lanza la excepción
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> reservaServicio.aceptarReserva(reservaId)
        );

        assertTrue(exception.getMessage().contains("Solo se pueden aceptar reservas pendientes"));
        verify(reservaRepositorio, never()).save(any(Reserva.class));
    }

    /**
     * Prueba rechazar reserva exitosamente
     */
    @Test
    void testRechazarReservaExitoso() throws Exception {
        // Sección de Arrange: Se definen los datos de la reserva a rechazar
        Long reservaId = 1L;
        String anfitrionId = "456";

        var huesped = new Usuario();
        huesped.setId("123");
        huesped.setNombre("Juan Perez");
        huesped.setEmail("juan@email.com");

        var anfitrion = new Usuario();
        anfitrion.setId(anfitrionId);

        var alojamiento = new Alojamiento();
        alojamiento.setId(1L);
        alojamiento.setAnfitrion(anfitrion);
        alojamiento.setTitulo("Casa en la playa");

        var reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setEstado(ReservaEstado.PENDIENTE);
        reserva.setAlojamiento(alojamiento);
        reserva.setHuesped(huesped);
        reserva.setFechaEntrada(LocalDate.now().plusDays(5));
        reserva.setFechaSalida(LocalDate.now().plusDays(10));

        User userDetails = new User(anfitrionId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reservaRepositorio.findById(reservaId)).thenReturn(Optional.of(reserva));
        when(reservaRepositorio.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(emailServicio).enviarEmail(any(EmailDTO.class));

        // Sección de Act: Ejecutar la acción de rechazar reserva
        reservaServicio.rechazarReserva(reservaId);

        // Sección de Assert: Verificar que se rechazó la reserva
        verify(reservaRepositorio, times(1)).findById(reservaId);
        verify(reservaRepositorio, times(1)).save(reserva);
        assertEquals(ReservaEstado.CANCELADA, reserva.getEstado());
        verify(emailServicio, times(1)).enviarEmail(any(EmailDTO.class));
    }

    /**
     * Prueba cancelar reserva exitosamente
     */
    @Test
    void testCancelarReservaExitoso() throws Exception {
        // Sección de Arrange: Se definen los datos de la reserva a cancelar
        Long reservaId = 1L;
        String huespedId = "123";

        var huesped = new Usuario();
        huesped.setId(huespedId);
        huesped.setNombre("Juan Perez");
        huesped.setEmail("juan@email.com");

        var anfitrion = new Usuario();
        anfitrion.setId("456");
        anfitrion.setNombre("Pedro Gomez");
        anfitrion.setEmail("pedro@email.com");

        var alojamiento = new Alojamiento();
        alojamiento.setId(1L);
        alojamiento.setAnfitrion(anfitrion);
        alojamiento.setTitulo("Casa en la playa");

        var reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setEstado(ReservaEstado.CONFIRMADA);
        reserva.setAlojamiento(alojamiento);
        reserva.setHuesped(huesped);
        reserva.setFechaEntrada(LocalDate.now().plusDays(10));
        reserva.setFechaSalida(LocalDate.now().plusDays(15));

        User userDetails = new User(huespedId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reservaRepositorio.findById(reservaId)).thenReturn(Optional.of(reserva));
        when(reservaRepositorio.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(emailServicio).enviarEmail(any(EmailDTO.class));

        // Sección de Act: Ejecutar la acción de cancelar reserva
        reservaServicio.cancelarReserva(reservaId);

        // Sección de Assert: Verificar que se canceló la reserva
        verify(reservaRepositorio, times(1)).findById(reservaId);
        verify(reservaRepositorio, times(1)).save(reserva);
        assertEquals(ReservaEstado.CANCELADA, reserva.getEstado());
        verify(emailServicio, times(2)).enviarEmail(any(EmailDTO.class));
    }

    /**
     * Prueba cancelar reserva sin permiso
     */
    @Test
    void testCancelarReservaSinPermiso() {
        // Sección de Arrange: Se definen los datos sin permiso
        Long reservaId = 1L;
        String usuarioId = "456";

        var huesped = new Usuario();
        huesped.setId("123");

        var reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setHuesped(huesped);

        User userDetails = new User(usuarioId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reservaRepositorio.findById(reservaId)).thenReturn(Optional.of(reserva));

        // Sección de Act & Assert: Verificar que se lanza la excepción
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> reservaServicio.cancelarReserva(reservaId)
        );

        assertEquals("No tienes permiso para cancelar esta reserva", exception.getMessage());
        verify(reservaRepositorio, never()).save(any(Reserva.class));
    }
}

