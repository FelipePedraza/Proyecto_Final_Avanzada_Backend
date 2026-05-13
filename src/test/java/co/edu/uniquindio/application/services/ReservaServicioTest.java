package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.EmailDTO;
import co.edu.uniquindio.application.dtos.pago.PagoIntentDTO;
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
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private AuthServicio authServicio;

    @Mock
    private PagoServicio pagoServicio;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private SimpleMeterRegistry meterRegistry;
    private ReservaServicioImpl reservaServicio;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        meterRegistry = new SimpleMeterRegistry();
        reservaServicio = new ReservaServicioImpl(
                reservaRepositorio, alojamientoRepositorio, usuarioRepositorio,
                reservaMapper, emailServicio, authServicio, pagoServicio, meterRegistry);
    }

    @Test
    void testCrearReservaExitoso() throws Exception {
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
        when(pagoServicio.crearIntentPago(anyLong(), anyLong(), anyString())).thenReturn(
                new PagoIntentDTO("secret", "pi_123", 50000L, "cop"));
        doNothing().when(emailServicio).enviarEmail(any(EmailDTO.class));

        reservaServicio.crear(creacionReservaDTO);

        verify(usuarioRepositorio, times(1)).findById(usuarioId);
        verify(alojamientoRepositorio, times(1)).findById(alojamientoId);
        verify(reservaMapper, times(1)).toEntity(creacionReservaDTO);
        verify(reservaRepositorio, times(2)).save(any(Reserva.class));
        verify(emailServicio, times(2)).enviarEmail(any(EmailDTO.class));
        assertEquals(1, meterRegistry.counter("reservas.creadas").count());
    }

    @Test
    void testCrearReservaUsuarioNoExiste() {
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

        NoFoundException exception = assertThrows(
                NoFoundException.class,
                () -> reservaServicio.crear(creacionReservaDTO)
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(usuarioRepositorio, times(1)).findById(usuarioId);
        verify(reservaRepositorio, never()).save(any(Reserva.class));
    }

    @Test
    void testCrearReservaAlojamientoNoExiste() {
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

        NoFoundException exception = assertThrows(
                NoFoundException.class,
                () -> reservaServicio.crear(creacionReservaDTO)
        );

        assertEquals("Alojamiento no encontrado", exception.getMessage());
        verify(alojamientoRepositorio, times(1)).findById(alojamientoId);
        verify(reservaRepositorio, never()).save(any(Reserva.class));
    }

    @Test
    void testCrearReservaFechasPasadas() {
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

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> reservaServicio.crear(creacionReservaDTO)
        );

        assertEquals("No se pueden reservar fechas pasadas", exception.getMessage());
        verify(reservaRepositorio, never()).save(any(Reserva.class));
    }

    @Test
    void testCrearReservaExcedeCapacidad() {
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

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> reservaServicio.crear(creacionReservaDTO)
        );

        assertTrue(exception.getMessage().contains("supera la capacidad máxima"));
        verify(reservaRepositorio, never()).save(any(Reserva.class));
    }

    @Test
    void testCrearReservaPropioAlojamiento() {
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
        alojamiento.setAnfitrion(huesped);

        User userDetails = new User(usuarioId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(usuarioRepositorio.findById(usuarioId)).thenReturn(Optional.of(huesped));
        when(alojamientoRepositorio.findById(alojamientoId)).thenReturn(Optional.of(alojamiento));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> reservaServicio.crear(creacionReservaDTO)
        );

        assertEquals("No puedes reservar tu propio alojamiento", exception.getMessage());
        verify(reservaRepositorio, never()).save(any(Reserva.class));
    }

    @Test
    void testAceptarReservaExitoso() throws Exception {
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
        reserva.setPagoEstado(co.edu.uniquindio.application.models.enums.PagoEstado.AUTORIZADO);
        reserva.setAlojamiento(alojamiento);
        reserva.setHuesped(huesped);
        reserva.setFechaEntrada(LocalDate.now().plusDays(5));
        reserva.setFechaSalida(LocalDate.now().plusDays(10));
        reserva.setCantidadHuespedes(2);
        reserva.setPrecio(500.0);
        reserva.setStripePaymentIntentId("pi_123");

        User userDetails = new User(anfitrionId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reservaRepositorio.findById(reservaId)).thenReturn(Optional.of(reserva));
        when(reservaRepositorio.findByAlojamiento_IdAndEstadoIn(anyLong(), any())).thenReturn(new ArrayList<>());
        when(reservaRepositorio.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(pagoServicio).capturarPago(anyString());
        doNothing().when(emailServicio).enviarEmail(any(EmailDTO.class));

        reservaServicio.aceptarReserva(reservaId);

        verify(reservaRepositorio, times(1)).findById(reservaId);
        verify(reservaRepositorio, times(1)).save(reserva);
        assertEquals(ReservaEstado.CONFIRMADA, reserva.getEstado());
        verify(emailServicio, times(2)).enviarEmail(any(EmailDTO.class));
        assertEquals(1, meterRegistry.counter("reservas.aceptadas").count());
    }

    @Test
    void testAceptarReservaSinPermiso() {
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

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> reservaServicio.aceptarReserva(reservaId)
        );

        assertEquals("Solo el anfitrión puede aceptar esta reserva", exception.getMessage());
        verify(reservaRepositorio, never()).save(any(Reserva.class));
    }

    @Test
    void testAceptarReservaNoPendiente() {
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

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> reservaServicio.aceptarReserva(reservaId)
        );

        assertTrue(exception.getMessage().contains("Solo se pueden aceptar reservas pendientes"));
        verify(reservaRepositorio, never()).save(any(Reserva.class));
    }

    @Test
    void testRechazarReservaExitoso() throws Exception {
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
        reserva.setStripePaymentIntentId("pi_123");

        User userDetails = new User(anfitrionId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reservaRepositorio.findById(reservaId)).thenReturn(Optional.of(reserva));
        when(reservaRepositorio.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(pagoServicio).cancelarPago(anyString());
        doNothing().when(emailServicio).enviarEmail(any(EmailDTO.class));

        reservaServicio.rechazarReserva(reservaId);

        verify(reservaRepositorio, times(1)).findById(reservaId);
        verify(reservaRepositorio, times(1)).save(reserva);
        assertEquals(ReservaEstado.CANCELADA, reserva.getEstado());
        verify(emailServicio, times(1)).enviarEmail(any(EmailDTO.class));
        assertEquals(1, meterRegistry.counter("reservas.rechazadas").count());
    }

    @Test
    void testCancelarReservaExitoso() throws Exception {
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
        reserva.setPagoEstado(co.edu.uniquindio.application.models.enums.PagoEstado.CAPTURADO);
        reserva.setStripePaymentIntentId("pi_123");

        User userDetails = new User(huespedId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(reservaRepositorio.findById(reservaId)).thenReturn(Optional.of(reserva));
        when(reservaRepositorio.save(any(Reserva.class))).thenReturn(reserva);
        doNothing().when(pagoServicio).reembolsarPago(anyString());
        doNothing().when(emailServicio).enviarEmail(any(EmailDTO.class));

        reservaServicio.cancelarReserva(reservaId);

        verify(reservaRepositorio, times(1)).findById(reservaId);
        verify(reservaRepositorio, times(1)).save(reserva);
        assertEquals(ReservaEstado.CANCELADA, reserva.getEstado());
        verify(emailServicio, times(2)).enviarEmail(any(EmailDTO.class));
        assertEquals(1, meterRegistry.counter("reservas.canceladas").count());
    }

    @Test
    void testCancelarReservaSinPermiso() {
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

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> reservaServicio.cancelarReserva(reservaId)
        );

        assertEquals("No tienes permiso para cancelar esta reserva", exception.getMessage());
        verify(reservaRepositorio, never()).save(any(Reserva.class));
    }
}
