package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.EmailDTO;
import co.edu.uniquindio.application.dtos.PageResponseDTO;
import co.edu.uniquindio.application.dtos.pago.PagoIntentDTO;
import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.CreacionReservaRespuestaDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ReservaDTO;
import co.edu.uniquindio.application.exceptions.NoFoundException;
import co.edu.uniquindio.application.exceptions.ValidationException;
import co.edu.uniquindio.application.mappers.ReservaMapper;
import co.edu.uniquindio.application.models.entitys.Alojamiento;
import co.edu.uniquindio.application.models.entitys.Reserva;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.models.enums.Estado;
import co.edu.uniquindio.application.models.enums.PagoEstado;
import co.edu.uniquindio.application.models.enums.ReservaEstado;
import co.edu.uniquindio.application.repositories.AlojamientoRepositorio;
import co.edu.uniquindio.application.repositories.ReservaRepositorio;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.services.*;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservaServicioImpl implements ReservaServicio {

    private final ReservaRepositorio reservaRepositorio;
    private final AlojamientoRepositorio alojamientoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final ReservaMapper reservaMapper;
    private final EmailServicio emailServicio;
    private final AuthServicio authServicio;
    private final PagoServicio pagoServicio;
    private final MeterRegistry meterRegistry;

    @Override
    public CreacionReservaRespuestaDTO crear(CreacionReservaDTO dto) throws Exception {

        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();

        if (!idUsuarioAutenticado.equals(dto.usuarioId())) {
            throw new AccessDeniedException("No puedes hacer reservas en nombre de otro usuario");
        }

        Usuario huesped = usuarioRepositorio.findById(dto.usuarioId())
                .orElseThrow(() -> new NoFoundException("Usuario no encontrado"));

        if (huesped.getEstado() == Estado.ELIMINADO) {
            throw new ValidationException("El usuario está inactivo");
        }

        Alojamiento alojamiento = alojamientoRepositorio.findById(dto.alojamientoId())
                .orElseThrow(() -> new NoFoundException("Alojamiento no encontrado"));

        if (alojamiento.getEstado() == Estado.ELIMINADO) {
            throw new ValidationException("El alojamiento no está disponible");
        }

        if (alojamiento.getAnfitrion().getId().equals(idUsuarioAutenticado)) {
            throw new ValidationException("No puedes reservar tu propio alojamiento");
        }

        validarFechas(dto.fechaEntrada(), dto.fechaSalida());

        if (dto.cantidadHuespedes() > alojamiento.getMaxHuespedes()) {
            throw new ValidationException(
                    "El número de huéspedes (" + dto.cantidadHuespedes() +
                            ") supera la capacidad máxima del alojamiento (" + alojamiento.getMaxHuespedes() + ")"
            );
        }

        if (existeSolapamiento(alojamiento.getId(), dto.fechaEntrada(), dto.fechaSalida(), null)) {
            throw new ValidationException("El alojamiento no está disponible en las fechas seleccionadas");
        }

        long numeroNoches = ChronoUnit.DAYS.between(dto.fechaEntrada(), dto.fechaSalida());
        double precioTotal = numeroNoches * alojamiento.getPrecioPorNoche();
        long precioEnCentavos = Math.round(precioTotal * 100);

        Reserva reserva = reservaMapper.toEntity(dto);
        reserva.setPrecio(precioTotal);
        reserva.setAlojamiento(alojamiento);
        reserva.setHuesped(huesped);
        reserva.setEstado(ReservaEstado.PENDIENTE);
        reserva.setPagoEstado(PagoEstado.PENDIENTE);
        reserva = reservaRepositorio.save(reserva);

        PagoIntentDTO pagoIntent;
        try {
            pagoIntent = pagoServicio.crearIntentPago(
                    precioEnCentavos,
                    reserva.getId(),
                    huesped.getEmail()
            );
        } catch (Exception e) {
            reservaRepositorio.delete(reserva);
            throw new Exception("No se pudo inicializar el pago. Intenta de nuevo.", e);
        }

        reserva.setStripePaymentIntentId(pagoIntent.paymentIntentId());
        reserva.setStripePrecioEnCentavos(precioEnCentavos);
        reservaRepositorio.save(reserva);

        enviarEmailsSolicitudReserva(reserva, alojamiento, huesped);
        meterRegistry.counter("reservas.creadas").increment();

        return new CreacionReservaRespuestaDTO(
                reserva.getId(),
                precioTotal,
                pagoIntent
        );
    }

    @Override
    public void aceptarReserva(Long id) throws Exception {

        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();

        Reserva reserva = reservaRepositorio.findById(id)
                .orElseThrow(() -> new NoFoundException("Reserva no encontrada"));

        if (!reserva.getAlojamiento().getAnfitrion().getId().equals(idUsuarioAutenticado)) {
            throw new AccessDeniedException("Solo el anfitrión puede aceptar esta reserva");
        }

        if (reserva.getEstado() != ReservaEstado.PENDIENTE) {
            throw new ValidationException("Solo se pueden aceptar reservas pendientes. Estado actual: " + reserva.getEstado());
        }

        if (reserva.getPagoEstado() != PagoEstado.AUTORIZADO) {
            throw new ValidationException(
                    "No se puede aceptar la reserva porque el huésped aún no ha completado el pago. " +
                            "Estado del pago: " + reserva.getPagoEstado()
            );
        }

        if (existeSolapamiento(reserva.getAlojamiento().getId(), reserva.getFechaEntrada(),
                reserva.getFechaSalida(), reserva.getId())) {
            throw new ValidationException("El alojamiento ya no está disponible en estas fechas");
        }

        pagoServicio.capturarPago(reserva.getStripePaymentIntentId());

        reserva.setEstado(ReservaEstado.CONFIRMADA);
        reserva.setPagoEstado(PagoEstado.CAPTURADO);
        reservaRepositorio.save(reserva);

        enviarEmailsConfirmacionReserva(reserva);
        meterRegistry.counter("reservas.aceptadas").increment();
    }

    @Override
    public void rechazarReserva(Long id) throws Exception {

        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();

        Reserva reserva = reservaRepositorio.findById(id)
                .orElseThrow(() -> new NoFoundException("Reserva no encontrada"));

        if (!reserva.getAlojamiento().getAnfitrion().getId().equals(idUsuarioAutenticado)) {
            throw new AccessDeniedException("Solo el anfitrión puede rechazar esta reserva");
        }

        if (reserva.getEstado() != ReservaEstado.PENDIENTE) {
            throw new ValidationException("Solo se pueden rechazar reservas pendientes");
        }

        if (reserva.getStripePaymentIntentId() != null) {
            pagoServicio.cancelarPago(reserva.getStripePaymentIntentId());
        }

        reserva.setEstado(ReservaEstado.CANCELADA);
        reserva.setPagoEstado(PagoEstado.CANCELADO);
        reservaRepositorio.save(reserva);

        enviarEmailsRechazoReserva(reserva);
        meterRegistry.counter("reservas.rechazadas").increment();
    }

    @Override
    public void cancelarReserva(Long id) throws Exception {

        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();

        Reserva reserva = reservaRepositorio.findById(id)
                .orElseThrow(() -> new NoFoundException("Reserva no encontrada"));

        if (!reserva.getHuesped().getId().equals(idUsuarioAutenticado)) {
            throw new AccessDeniedException("No tienes permiso para cancelar esta reserva");
        }

        if (reserva.getEstado() != ReservaEstado.CONFIRMADA &&
                reserva.getEstado() != ReservaEstado.PENDIENTE) {
            throw new ValidationException("Solo se pueden cancelar reservas confirmadas o pendientes");
        }

        LocalDateTime fechaLimite = reserva.getFechaEntrada().atStartOfDay().minusHours(48);
        if (LocalDateTime.now().isAfter(fechaLimite)) {
            throw new ValidationException(
                    "No se puede cancelar la reserva. Debe hacerlo con al menos 48 horas de anticipación"
            );
        }

        if (reserva.getStripePaymentIntentId() != null) {
            if (reserva.getPagoEstado() == PagoEstado.CAPTURADO) {
                pagoServicio.reembolsarPago(reserva.getStripePaymentIntentId());
                reserva.setPagoEstado(PagoEstado.REEMBOLSADO);
            } else if (reserva.getPagoEstado() == PagoEstado.AUTORIZADO) {
                pagoServicio.cancelarPago(reserva.getStripePaymentIntentId());
                reserva.setPagoEstado(PagoEstado.CANCELADO);
            }
        }

        reserva.setEstado(ReservaEstado.CANCELADA);
        reservaRepositorio.save(reserva);

        enviarEmailsCancelacion(reserva);
        meterRegistry.counter("reservas.canceladas").increment();
    }

    @Override
    public PageResponseDTO<ItemReservaDTO> obtenerReservasUsuario(String id, ReservaEstado estado, LocalDate fechaEntrada, LocalDate fechaSalida, Pageable pageable) throws Exception {

        if (!authServicio.obtnerIdAutenticado(id)) {
            throw new AccessDeniedException("No tiene permisos para las reservas de este usuario.");
        }

        Page<ItemReservaDTO> reservas = reservaRepositorio.buscarConFiltrosUsuario(id, estado, fechaEntrada, fechaSalida, pageable)
                .map(reservaMapper::toItemDTO);

        return PageResponseDTO.fromPage(reservas);
    }

    @Override
    public PageResponseDTO<ReservaDTO> obtenerReservasAlojamiento(Long idAlojamiento, ReservaEstado estado, LocalDate fechaEntrada, LocalDate fechaSalida, Pageable pageable) throws Exception {

        Optional<Alojamiento> alojamientoOptional = alojamientoRepositorio.findById(idAlojamiento);

        if (alojamientoOptional.isEmpty()) {
            throw new NoFoundException("Alojamiento no encontrado");
        }

        Page<ReservaDTO> reservas = reservaRepositorio.buscarConFiltrosAlojamiento(idAlojamiento, estado, fechaEntrada, fechaSalida, pageable)
                .map(reservaMapper::toDTO);

        return PageResponseDTO.fromPage(reservas);
    }

    private void validarFechas(LocalDate fechaEntrada, LocalDate fechaSalida) throws ValidationException {
        LocalDate hoy = LocalDate.now();

        if (fechaEntrada.isBefore(hoy)) {
            throw new ValidationException("No se pueden reservar fechas pasadas");
        }

        if (fechaSalida.isBefore(fechaEntrada) || fechaSalida.isEqual(fechaEntrada)) {
            throw new ValidationException("La fecha de salida debe ser posterior a la fecha de entrada");
        }

        long noches = ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
        if (noches < 1) {
            throw new ValidationException("La reserva debe ser de al menos 1 noche");
        }
    }

    private boolean existeSolapamiento(Long alojamientoId, LocalDate fechaEntrada,
                                       LocalDate fechaSalida, Long reservaIdExcluir) {

        List<Reserva> reservasExistentes = reservaRepositorio
                .findByAlojamiento_IdAndEstadoIn(
                        alojamientoId,
                        List.of(ReservaEstado.CONFIRMADA, ReservaEstado.PENDIENTE)
                );

        for (Reserva reserva : reservasExistentes) {
            if (reservaIdExcluir != null && reserva.getId().equals(reservaIdExcluir)) {
                continue;
            }

            boolean haySolapamiento = !(fechaSalida.isBefore(reserva.getFechaEntrada()) ||
                    fechaEntrada.isAfter(reserva.getFechaSalida()));

            if (haySolapamiento) {
                return true;
            }
        }

        return false;
    }

    private void enviarEmailsSolicitudReserva(Reserva reserva, Alojamiento alojamiento, Usuario huesped) {
        String asuntoHuesped = "Solicitud de reserva enviada - " + alojamiento.getTitulo();
        String cuerpoHuesped = String.format(
                "¡Hola %s!\n\n" +
                        "Tu solicitud de reserva ha sido enviada al anfitrión.\n\n" +
                        "Detalles:\n" +
                        "- Alojamiento: %s\n" +
                        "- Check-in: %s\n" +
                        "- Check-out: %s\n" +
                        "- Huéspedes: %d\n" +
                        "- Precio total: $%.2f\n\n" +
                        "Estado: PENDIENTE DE APROBACIÓN\n\n" +
                        "El anfitrión revisará tu solicitud y te notificaremos cuando la acepte o rechace.",
                huesped.getNombre(),
                alojamiento.getTitulo(),
                reserva.getFechaEntrada(),
                reserva.getFechaSalida(),
                reserva.getCantidadHuespedes(),
                reserva.getPrecio()
        );

        try {
            emailServicio.enviarEmail(new EmailDTO(asuntoHuesped, cuerpoHuesped, huesped.getEmail()));
        } catch (Exception e) {
            System.err.println("Error enviando email al huésped: " + e.getMessage());
        }

        String asuntoAnfitrion = "Nueva solicitud de reserva - " + alojamiento.getTitulo();
        String cuerpoAnfitrion = String.format(
                "¡Hola %s!\n\n" +
                        "Tienes una nueva solicitud de reserva que requiere tu aprobación.\n\n" +
                        "Detalles:\n" +
                        "- Alojamiento: %s\n" +
                        "- Huésped: %s\n" +
                        "- Check-in: %s\n" +
                        "- Check-out: %s\n" +
                        "- Número de huéspedes: %d\n" +
                        "- Precio total: $%.2f\n\n" +
                        "Por favor, revisa la solicitud y acéptala o recházala desde tu panel de anfitrión.",
                alojamiento.getAnfitrion().getNombre(),
                alojamiento.getTitulo(),
                huesped.getNombre(),
                reserva.getFechaEntrada(),
                reserva.getFechaSalida(),
                reserva.getCantidadHuespedes(),
                reserva.getPrecio()
        );

        try {
            emailServicio.enviarEmail(new EmailDTO(asuntoAnfitrion, cuerpoAnfitrion,
                    alojamiento.getAnfitrion().getEmail()));
        } catch (Exception e) {
            System.err.println("Error enviando email al anfitrión: " + e.getMessage());
        }
    }

    private void enviarEmailsConfirmacionReserva(Reserva reserva) {
        String asuntoHuesped = "¡Reserva confirmada! - " + reserva.getAlojamiento().getTitulo();
        String cuerpoHuesped = String.format(
                "¡Hola %s!\n\n" +
                        "¡Buenas noticias! El anfitrión ha aceptado tu reserva.\n\n" +
                        "Detalles:\n" +
                        "- Alojamiento: %s\n" +
                        "- Check-in: %s\n" +
                        "- Check-out: %s\n" +
                        "- Huéspedes: %d\n" +
                        "- Precio total: $%.2f\n\n" +
                        "Estado: CONFIRMADA\n\n" +
                        "¡Esperamos que disfrutes tu estadía!",
                reserva.getHuesped().getNombre(),
                reserva.getAlojamiento().getTitulo(),
                reserva.getFechaEntrada(),
                reserva.getFechaSalida(),
                reserva.getCantidadHuespedes(),
                reserva.getPrecio()
        );

        try {
            emailServicio.enviarEmail(new EmailDTO(asuntoHuesped, cuerpoHuesped,
                    reserva.getHuesped().getEmail()));
        } catch (Exception e) {
            System.err.println("Error enviando email de confirmación al huésped: " + e.getMessage());
        }

        String asuntoAnfitrion = "Reserva confirmada - " + reserva.getAlojamiento().getTitulo();
        String cuerpoAnfitrion = String.format(
                "Hola %s,\n\n" +
                        "Has aceptado la reserva de %s.\n\n" +
                        "Detalles:\n" +
                        "- Alojamiento: %s\n" +
                        "- Check-in: %s\n" +
                        "- Check-out: %s\n" +
                        "- Número de huéspedes: %d\n\n" +
                        "Recuerda preparar todo para la llegada de tu huésped.",
                reserva.getAlojamiento().getAnfitrion().getNombre(),
                reserva.getHuesped().getNombre(),
                reserva.getAlojamiento().getTitulo(),
                reserva.getFechaEntrada(),
                reserva.getFechaSalida(),
                reserva.getCantidadHuespedes()
        );

        try {
            emailServicio.enviarEmail(new EmailDTO(asuntoAnfitrion, cuerpoAnfitrion,
                    reserva.getAlojamiento().getAnfitrion().getEmail()));
        } catch (Exception e) {
            System.err.println("Error enviando email de confirmación al anfitrión: " + e.getMessage());
        }
    }

    private void enviarEmailsRechazoReserva(Reserva reserva) {
        String asuntoHuesped = "Solicitud de reserva rechazada - " + reserva.getAlojamiento().getTitulo();
        String cuerpoHuesped = String.format(
                "Hola %s,\n\n" +
                        "Lamentablemente, el anfitrión no pudo aceptar tu solicitud de reserva para '%s'.\n\n" +
                        "Fechas solicitadas: %s al %s\n\n" +
                        "Te invitamos a buscar otros alojamientos disponibles que se ajusten a tus necesidades.\n\n" +
                        "¡Gracias por usar ViviGo!",
                reserva.getHuesped().getNombre(),
                reserva.getAlojamiento().getTitulo(),
                reserva.getFechaEntrada(),
                reserva.getFechaSalida()
        );

        try {
            emailServicio.enviarEmail(new EmailDTO(asuntoHuesped, cuerpoHuesped,
                    reserva.getHuesped().getEmail()));
        } catch (Exception e) {
            System.err.println("Error enviando email de rechazo al huésped: " + e.getMessage());
        }
    }

    private void enviarEmailsCancelacion(Reserva reserva) {
        String asuntoHuesped = "Reserva cancelada - " + reserva.getAlojamiento().getTitulo();
        String cuerpoHuesped = String.format(
                "Hola %s,\n\n" +
                        "Tu reserva ha sido cancelada.\n\n" +
                        "Detalles de la reserva cancelada:\n" +
                        "- Alojamiento: %s\n" +
                        "- Fechas: %s al %s\n\n" +
                        "Si tienes alguna pregunta, no dudes en contactarnos.",
                reserva.getHuesped().getNombre(),
                reserva.getAlojamiento().getTitulo(),
                reserva.getFechaEntrada(),
                reserva.getFechaSalida()
        );

        try {
            emailServicio.enviarEmail(new EmailDTO(asuntoHuesped, cuerpoHuesped,
                    reserva.getHuesped().getEmail()));
        } catch (Exception e) {
            System.err.println("Error enviando email al huésped: " + e.getMessage());
        }

        String asuntoAnfitrion = "Reserva cancelada - " + reserva.getAlojamiento().getTitulo();
        String cuerpoAnfitrion = String.format(
                "Hola %s,\n\n" +
                        "Una reserva ha sido cancelada.\n\n" +
                        "Detalles:\n" +
                        "- Alojamiento: %s\n" +
                        "- Huésped: %s\n" +
                        "- Fechas: %s al %s",
                reserva.getAlojamiento().getAnfitrion().getNombre(),
                reserva.getAlojamiento().getTitulo(),
                reserva.getHuesped().getNombre(),
                reserva.getFechaEntrada(),
                reserva.getFechaSalida()
        );

        try {
            emailServicio.enviarEmail(new EmailDTO(asuntoAnfitrion, cuerpoAnfitrion,
                    reserva.getAlojamiento().getAnfitrion().getEmail()));
        } catch (Exception e) {
            System.err.println("Error enviando email al anfitrión: " + e.getMessage());
        }
    }
}
