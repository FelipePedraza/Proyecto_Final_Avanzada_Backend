package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.PageResponseDTO;
import co.edu.uniquindio.application.dtos.alojamiento.*;
import co.edu.uniquindio.application.dtos.usuario.UsuarioDTO;
import co.edu.uniquindio.application.exceptions.NoFoundException;
import co.edu.uniquindio.application.exceptions.ValidationException;
import co.edu.uniquindio.application.models.entitys.Alojamiento;
import co.edu.uniquindio.application.models.enums.ReservaEstado;
import co.edu.uniquindio.application.models.enums.Servicio;
import co.edu.uniquindio.application.repositories.AlojamientoRepositorio;
import co.edu.uniquindio.application.repositories.ReservaRepositorio;
import co.edu.uniquindio.application.services.AlojamientoServicio;
import co.edu.uniquindio.application.services.AuthServicio;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;
import co.edu.uniquindio.application.mappers.AlojamientoMapper;
import co.edu.uniquindio.application.mappers.UsuarioMapper;
import co.edu.uniquindio.application.models.entitys.Usuario;
import org.springframework.security.access.AccessDeniedException;
import co.edu.uniquindio.application.services.UsuarioServicio;
import co.edu.uniquindio.application.services.ImagenServicio;
import co.edu.uniquindio.application.models.enums.Estado;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AlojamientoServicioImpl implements AlojamientoServicio {

    private final AlojamientoRepositorio alojamientoRepositorio;
    private final AlojamientoMapper alojamientoMapper;
    private final UsuarioServicio usuarioServicio;
    private final UsuarioMapper usuarioMapper;
    private final ImagenServicio imagenServicio;
    private final AuthServicio authServicio;
    private final ReservaRepositorio reservaRepositorio;
    private final MeterRegistry meterRegistry;

    @Override
    public void crear(CreacionAlojamientoDTO alojamientoDTO) throws Exception {

        if(existePorTitulo(alojamientoDTO.titulo())){
            throw new Exception("El titulo ya existe");
        }

        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();
        UsuarioDTO usuarioDTO = usuarioServicio.obtener(idUsuarioAutenticado);
        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);

        boolean esAnfitrion = usuario.getEsAnfitrion() != null && usuario.getEsAnfitrion();

        if (!esAnfitrion) {
            throw new AccessDeniedException("El usuario no es un anfitrion");
        }

        List<String> urlsSeguras = alojamientoDTO.imagenes();
        if (urlsSeguras == null || urlsSeguras.isEmpty()) {
            throw new Exception("El alojamiento debe tener al menos una imagen");
        }

        Alojamiento nuevoAlojamiento = alojamientoMapper.toEntity(alojamientoDTO);
        nuevoAlojamiento.setImagenes(urlsSeguras);
        nuevoAlojamiento.setAnfitrion(usuario);
        alojamientoRepositorio.save(nuevoAlojamiento);
        meterRegistry.counter("alojamientos.creados").increment();
    }

    @Override
    public void editar (Long id, EdicionAlojamientoDTO edicionAlojamientoDTO) throws Exception {
        Alojamiento alojamiento = obtenerAlojamientoId(id);

        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();
        UsuarioDTO usuarioDTO = usuarioServicio.obtener(idUsuarioAutenticado);
        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);

        boolean esAnfitrion = usuario.getEsAnfitrion() != null && usuario.getEsAnfitrion();

        if (!esAnfitrion) {
            throw new AccessDeniedException("El usuario no es un anfitrion");
        }

        if (alojamiento.getAnfitrion() == null || !alojamiento.getAnfitrion().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("No tiene permiso para editar este alojamiento");
        }

        List<String> actuales = alojamiento.getImagenes() != null ? new ArrayList<>(alojamiento.getImagenes()) : new ArrayList<>();

        if (edicionAlojamientoDTO.titulo() != null && !edicionAlojamientoDTO.titulo().equalsIgnoreCase(alojamiento.getTitulo())) {
            if (existePorTitulo(edicionAlojamientoDTO.titulo())) {
                throw new Exception("El título ya existe");
            }
        }

        alojamientoMapper.updateAlojamientoFromDto(edicionAlojamientoDTO, alojamiento);

        List<String> finalImgs = edicionAlojamientoDTO.imagenes() != null ? new ArrayList<>(edicionAlojamientoDTO.imagenes()) : new ArrayList<>();

        if (finalImgs.isEmpty()) {
            throw new Exception("El alojamiento debe tener al menos una imagen");
        }

        alojamiento.setImagenes(finalImgs);
        try {
            alojamientoRepositorio.save(alojamiento);
        } catch (Exception bdEx) {
            throw new Exception("Error guardando alojamiento (BD).", bdEx);
        }

        List<String> aEliminar = new ArrayList<>();
        for (String urlActual : actuales) {
            if (!finalImgs.contains(urlActual)) {
                aEliminar.add(urlActual);
            }
        }

        for (String url : aEliminar) {
            try {
                String publicId = imagenServicio.extraerPublicIdDelUrl(url);
                if (publicId != null && !publicId.isBlank()) {
                    imagenServicio.eliminar(publicId);
                } else {
                    imagenServicio.eliminar(url);
                }
            } catch (Exception ignored) {
                System.err.println("No se pudo eliminar la imagen antigua: " + url + ". Error: " + ignored.getMessage());
            }
        }
    }

    @Override
    public void eliminar(Long id) throws Exception {
        Alojamiento alojamiento = obtenerAlojamientoId(id);

        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();
        UsuarioDTO usuarioDTO = usuarioServicio.obtener(idUsuarioAutenticado);
        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);

        boolean esAnfitrion = usuario.getEsAnfitrion() != null && usuario.getEsAnfitrion();

        if (!esAnfitrion) {
            throw new AccessDeniedException("El usuario no es un anfitrion");
        }

        if (alojamiento.getAnfitrion() == null || !alojamiento.getAnfitrion().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("No tiene permiso para eliminar este alojamiento");
        }

        if (alojamiento.getReservas().isEmpty()) {
            alojamiento.setEstado(Estado.ELIMINADO);
            alojamientoRepositorio.save(alojamiento);
            meterRegistry.counter("alojamientos.eliminados").increment();
        } else if (reservaRepositorio.countByAlojamiento_IdAndEstadoIn(id, List.of(ReservaEstado.CONFIRMADA, ReservaEstado.PENDIENTE)) == 0) {
            alojamiento.setEstado(Estado.ELIMINADO);
            alojamientoRepositorio.save(alojamiento);
            meterRegistry.counter("alojamientos.eliminados").increment();
        } else{
            throw new ValidationException("No puede eliminar un alojamiento con reservas");
        }
    }

    @Override
    public AlojamientoDTO obtenerPorId(Long id) throws Exception {
        Alojamiento alojamiento = obtenerAlojamientoId(id);
        return alojamientoMapper.toDTO(alojamiento);
    }

    @Override
    public MetricasDTO obtenerMetricas(Long id) throws Exception {

        Alojamiento alojamiento = obtenerAlojamientoId(id);

        Integer totalResenas = alojamiento.getNumeroCalificaciones() != null ?
                alojamiento.getNumeroCalificaciones() : 0;

        Double promedioCalificaciones = alojamiento.getPromedioCalificaciones() != null ?
                alojamiento.getPromedioCalificaciones() : 0.0;

        long totalReservas = reservaRepositorio.countByAlojamiento_IdAndEstadoIn(
                id,
                List.of(ReservaEstado.CONFIRMADA, ReservaEstado.COMPLETADA)
        );

        return new MetricasDTO(totalResenas, promedioCalificaciones, totalReservas);
    }

    @Override
    public PageResponseDTO<ItemAlojamientoDTO> obtenerAlojamientos(AlojamientoFiltroDTO filtros, Pageable pageable) throws Exception {

        if (filtros.fechaEntrada() != null && filtros.fechaSalida() != null) {
            if (filtros.fechaEntrada().isAfter(filtros.fechaSalida())) {
                throw new ValidationException("La fecha de entrada no puede ser posterior a la fecha de salida");
            }

            if (filtros.fechaEntrada().isBefore(LocalDate.now())) {
                throw new ValidationException("La fecha de entrada no puede ser anterior a hoy");
            }
        }

        if (filtros.precioMin() != null && filtros.precioMax() != null) {
            if (filtros.precioMin() > filtros.precioMax()) {
                throw new ValidationException("El precio mínimo no puede ser mayor al precio máximo");
            }
        }

        if (filtros.huespedes() != null && filtros.huespedes() < 1) {
            throw new ValidationException("El número de huéspedes debe ser al menos 1");
        }

        List<Servicio> servicios = filtros.servicios();
        Long cantidadServicios = (servicios != null && !servicios.isEmpty())
                ? (long) servicios.size()
                : 0L;

        Page<ItemAlojamientoDTO> alojamientos = alojamientoRepositorio.buscarConFiltros(
                filtros.ciudad(),
                filtros.fechaEntrada(),
                filtros.fechaSalida(),
                filtros.huespedes(),
                filtros.precioMin(),
                filtros.precioMax(),
                servicios,
                cantidadServicios,
                Estado.ACTIVO,
                pageable
        ).map(alojamientoMapper::toItemDTO);

        return PageResponseDTO.fromPage(alojamientos);
    }

    @Override
    public PageResponseDTO<ItemAlojamientoDTO> obtenerAlojamientosUsuario(String id, Pageable pageable) throws Exception {

        if (!authServicio.obtnerIdAutenticado(id)) {
            throw new AccessDeniedException("No tiene permisos para ver los alojamientos de este usuario.");
        }

        Page<ItemAlojamientoDTO> alojamientos = alojamientoRepositorio.getAlojamientos(id, Estado.ACTIVO, pageable)
                .map(alojamientoMapper::toItemDTO);

        return PageResponseDTO.fromPage(alojamientos);
    }

    @Override
    public PageResponseDTO<ItemAlojamientoDTO> sugerirAlojamientos(String ciudad, Pageable pageable) {
        String ciudadBusqueda = (ciudad == null || ciudad.isBlank()) ? null : ciudad.trim();

        Page<ItemAlojamientoDTO> alojamientos = alojamientoRepositorio.sugerirPorCiudad(ciudadBusqueda, Estado.ACTIVO, pageable)
                .map(alojamientoMapper::toItemDTO);

        return PageResponseDTO.fromPage(alojamientos);
    }

    public boolean existePorTitulo(String titulo){

        Optional<Alojamiento> optionalAlojamiento = alojamientoRepositorio.findByTitulo(titulo);

        return optionalAlojamiento.isPresent();
    }

    public Alojamiento obtenerAlojamientoId(Long id) throws Exception {
        Optional<Alojamiento> optionalAlojamiento = alojamientoRepositorio.findById(id);

        if(optionalAlojamiento.isEmpty() ||  optionalAlojamiento.get().getEstado().equals(Estado.ELIMINADO)) {
            throw new NoFoundException("No se encontro el alojamiento con el id: " + id);
        }

        return optionalAlojamiento.get();
    }

}
