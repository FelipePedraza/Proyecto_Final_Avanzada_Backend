package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.alojamiento.*;
import co.edu.uniquindio.application.dtos.usuario.UsuarioDTO;
import co.edu.uniquindio.application.exceptions.NoFoundException;
import co.edu.uniquindio.application.exceptions.ValidationException;
import co.edu.uniquindio.application.mappers.AlojamientoMapper;
import co.edu.uniquindio.application.mappers.UsuarioMapper;
import co.edu.uniquindio.application.models.entitys.Alojamiento;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.models.enums.Ciudad;
import co.edu.uniquindio.application.models.enums.Estado;
import co.edu.uniquindio.application.models.enums.Rol;
import co.edu.uniquindio.application.models.enums.Servicio;
import co.edu.uniquindio.application.repositories.AlojamientoRepositorio;
import co.edu.uniquindio.application.repositories.ReservaRepositorio;
import co.edu.uniquindio.application.services.impl.AlojamientoServicioImpl;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
public class AlojamientoServicioTest {

    @Mock
    private AlojamientoRepositorio alojamientoRepositorio;

    @Mock
    private AlojamientoMapper alojamientoMapper;

    @Mock
    private UsuarioServicio usuarioServicio;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private ImagenServicio imagenServicio;

    @Mock
    private AuthServicio authServicio;

    @Mock
    private ReservaRepositorio reservaRepositorio;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private SimpleMeterRegistry meterRegistry;
    private AlojamientoServicioImpl alojamientoServicio;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        meterRegistry = new SimpleMeterRegistry();
        alojamientoServicio = new AlojamientoServicioImpl(
                alojamientoRepositorio, alojamientoMapper, usuarioServicio,
                usuarioMapper, imagenServicio, authServicio, reservaRepositorio, meterRegistry);
    }

    @Test
    void testObtenerAlojamientoPorIdExitoso() throws Exception {
        Long id = 1L;
        var alojamiento = new Alojamiento();
        alojamiento.setId(id);
        alojamiento.setTitulo("Casa en la playa");
        alojamiento.setPrecioPorNoche(100.0f);
        alojamiento.setEstado(Estado.ACTIVO);

        var localizacion = new LocalizacionDTO(10.4, -75.5);
        var direccion = new DireccionDTO(
                Ciudad.OTRA,
                "Calle 123",
                localizacion
        );

        var alojamientoDTO = new AlojamientoDTO(
                id,
                "Casa en la playa",
                "Hermosa casa frente al mar",
                direccion,
                100.0f,
                4,
                List.of(Servicio.WIFI, Servicio.PISCINA),
                List.of("http://imagen1.url"),
                "Pedro Gomez",
                "123"
        );

        when(alojamientoRepositorio.findById(id)).thenReturn(Optional.of(alojamiento));
        when(alojamientoMapper.toDTO(alojamiento)).thenReturn(alojamientoDTO);

        AlojamientoDTO resultado = alojamientoServicio.obtenerPorId(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.id());
        assertEquals("Casa en la playa", resultado.titulo());
        verify(alojamientoRepositorio, times(1)).findById(id);
        verify(alojamientoMapper, times(1)).toDTO(alojamiento);
    }

    @Test
    void testObtenerAlojamientoNoExiste() {
        Long id = 999L;
        when(alojamientoRepositorio.findById(id)).thenReturn(Optional.empty());

        NoFoundException exception = assertThrows(
                NoFoundException.class,
                () -> alojamientoServicio.obtenerPorId(id)
        );

        assertEquals("No se encontro el alojamiento con el id: " + id, exception.getMessage());
        verify(alojamientoRepositorio, times(1)).findById(id);
    }

    @Test
    void testEliminarAlojamientoExitoso() throws Exception {
        Long id = 1L;
        String usuarioId = "123";

        var usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setEsAnfitrion(true);

        var alojamiento = new Alojamiento();
        alojamiento.setId(id);
        alojamiento.setEstado(Estado.ACTIVO);
        alojamiento.setAnfitrion(usuario);
        alojamiento.setReservas(new ArrayList<>());

        var usuarioDTO = new UsuarioDTO(
                usuarioId,
                "Pedro Gomez",
                "pedro@email.com",
                "123456789",
                Rol.Anfitrion,
                LocalDate.of(1990, 1, 1),
                "http://photo.url",
                true
        );

        User userDetails = new User(usuarioId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(alojamientoRepositorio.findById(id)).thenReturn(Optional.of(alojamiento));
        when(usuarioServicio.obtener(usuarioId)).thenReturn(usuarioDTO);
        when(usuarioMapper.toEntity(usuarioDTO)).thenReturn(usuario);
        when(alojamientoRepositorio.save(any(Alojamiento.class))).thenReturn(alojamiento);

        alojamientoServicio.eliminar(id);

        verify(alojamientoRepositorio, times(1)).findById(id);
        verify(alojamientoRepositorio, times(1)).save(alojamiento);
        assertEquals(Estado.ELIMINADO, alojamiento.getEstado());
        assertEquals(1, meterRegistry.counter("alojamientos.eliminados").count());
    }

    @Test
    void testEliminarAlojamientoSinPermisos() throws Exception {
        Long id = 1L;
        String usuarioId = "123";
        String otroUsuarioId = "456";

        var usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setEsAnfitrion(true);

        var otroUsuario = new Usuario();
        otroUsuario.setId(otroUsuarioId);
        otroUsuario.setEsAnfitrion(true);

        var alojamiento = new Alojamiento();
        alojamiento.setId(id);
        alojamiento.setAnfitrion(usuario);
        alojamiento.setEstado(Estado.ACTIVO);
        alojamiento.setReservas(new ArrayList<>());

        var usuarioDTO = new UsuarioDTO(
                otroUsuarioId,
                "Juan Perez",
                "juan@email.com",
                "123456789",
                Rol.Anfitrion,
                LocalDate.of(1990, 1, 1),
                "http://photo.url",
                true
        );

        User userDetails = new User(otroUsuarioId, "password", Collections.emptyList());

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(alojamientoRepositorio.findById(id)).thenReturn(Optional.of(alojamiento));
        when(usuarioServicio.obtener(otroUsuarioId)).thenReturn(usuarioDTO);
        when(usuarioMapper.toEntity(usuarioDTO)).thenReturn(otroUsuario);

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> alojamientoServicio.eliminar(id)
        );

        assertEquals("No tiene permiso para eliminar este alojamiento", exception.getMessage());
        verify(alojamientoRepositorio, never()).save(any(Alojamiento.class));
    }

    @Test
    void testObtenerAlojamientosConFiltros() throws Exception {
        LocalDate fechaEntrada = LocalDate.now().plusDays(5);
        LocalDate fechaSalida = LocalDate.now().plusDays(10);

        var filtros = new AlojamientoFiltroDTO(
                "Cartagena",
                fechaEntrada,
                fechaSalida,
                2,
                50.0f,
                200.0f,
                List.of()
        );

        List<Alojamiento> alojamientos = new ArrayList<>();
        var alojamiento = new Alojamiento();
        alojamiento.setId(1L);
        alojamiento.setTitulo("Casa en la playa");
        alojamientos.add(alojamiento);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Alojamiento> page = new PageImpl<>(alojamientos, pageable, alojamientos.size());

        when(alojamientoRepositorio.buscarConFiltros(
                eq("Cartagena"),
                eq(fechaEntrada),
                eq(fechaSalida),
                eq(2),
                eq(50.0f),
                eq(200.0f),
                any(),
                anyLong(),
                eq(Estado.ACTIVO),
                any(Pageable.class)
        )).thenReturn(page);

        var resultado = alojamientoServicio.obtenerAlojamientos(filtros, pageable);

        assertNotNull(resultado);
        verify(alojamientoRepositorio, times(1)).buscarConFiltros(
                eq("Cartagena"),
                eq(fechaEntrada),
                eq(fechaSalida),
                eq(2),
                eq(50.0f),
                eq(200.0f),
                any(),
                anyLong(),
                eq(Estado.ACTIVO),
                any(Pageable.class)
        );
    }

    @Test
    void testObtenerAlojamientosFechaEntradaPosteriorASalida() {
        LocalDate fechaEntrada = LocalDate.now().plusDays(10);
        LocalDate fechaSalida = LocalDate.now().plusDays(5);

        var filtros = new AlojamientoFiltroDTO(
                "Cartagena",
                fechaEntrada,
                fechaSalida,
                2,
                50.0f,
                200.0f,
                List.of()
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> alojamientoServicio.obtenerAlojamientos(filtros, PageRequest.of(0,10))
        );

        assertEquals("La fecha de entrada no puede ser posterior a la fecha de salida", exception.getMessage());
        verify(alojamientoRepositorio, never()).buscarConFiltros(
                anyString(),
                any(LocalDate.class),
                any(LocalDate.class),
                anyInt(),
                anyFloat(),
                anyFloat(),
                any(),
                anyLong(),
                any(Estado.class),
                any(Pageable.class)
        );
    }

    @Test
    void testObtenerAlojamientosFechaEntradaPasada() {
        LocalDate fechaEntrada = LocalDate.now().minusDays(5);
        LocalDate fechaSalida = LocalDate.now().plusDays(5);

        var filtros = new AlojamientoFiltroDTO(
                "Cartagena",
                fechaEntrada,
                fechaSalida,
                2,
                50.0f,
                200.0f,
                List.of()
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> alojamientoServicio.obtenerAlojamientos(filtros, PageRequest.of(0,10))
        );

        assertEquals("La fecha de entrada no puede ser anterior a hoy", exception.getMessage());
        verify(alojamientoRepositorio, never()).buscarConFiltros(
                anyString(),
                any(LocalDate.class),
                any(LocalDate.class),
                anyInt(),
                anyFloat(),
                anyFloat(),
                any(),
                anyLong(),
                any(Estado.class),
                any(Pageable.class)
        );
    }

    @Test
    void testObtenerAlojamientosPrecioMinMayorQuePrecioMax() {
        var filtros = new AlojamientoFiltroDTO(
                "Cartagena",
                null,
                null,
                2,
                200.0f,
                50.0f,
                List.of()
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> alojamientoServicio.obtenerAlojamientos(filtros, PageRequest.of(0,10))
        );

        assertEquals("El precio mínimo no puede ser mayor al precio máximo", exception.getMessage());
        verify(alojamientoRepositorio, never()).buscarConFiltros(
                anyString(),
                any(),
                any(),
                anyInt(),
                anyFloat(),
                anyFloat(),
                any(),
                anyLong(),
                any(Estado.class),
                any(Pageable.class)
        );
    }

    @Test
    void testObtenerAlojamientosHuespedesInvalido() {
        var filtros = new AlojamientoFiltroDTO(
                "Cartagena",
                null,
                null,
                0,
                50.0f,
                200.0f,
                List.of()
        );

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> alojamientoServicio.obtenerAlojamientos(filtros, PageRequest.of(0,10))
        );

        assertEquals("El número de huéspedes debe ser al menos 1", exception.getMessage());
        verify(alojamientoRepositorio, never()).buscarConFiltros(
                anyString(),
                any(),
                any(),
                anyInt(),
                anyFloat(),
                anyFloat(),
                any(),
                anyLong(),
                any(Estado.class),
                any(Pageable.class)
        );
    }

    @Test
    void testExistePorTitulo() {
        String titulo = "Casa en la playa";
        var alojamiento = new Alojamiento();
        alojamiento.setTitulo(titulo);

        when(alojamientoRepositorio.findByTitulo(titulo)).thenReturn(Optional.of(alojamiento));

        boolean existe = alojamientoServicio.existePorTitulo(titulo);

        assertTrue(existe);
        verify(alojamientoRepositorio, times(1)).findByTitulo(titulo);
    }

    @Test
    void testNoExistePorTitulo() {
        String titulo = "Alojamiento inexistente";

        when(alojamientoRepositorio.findByTitulo(titulo)).thenReturn(Optional.empty());

        boolean existe = alojamientoServicio.existePorTitulo(titulo);

        assertFalse(existe);
        verify(alojamientoRepositorio, times(1)).findByTitulo(titulo);
    }
}
