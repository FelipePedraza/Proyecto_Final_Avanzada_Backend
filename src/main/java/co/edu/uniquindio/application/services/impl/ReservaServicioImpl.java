package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.EmailDTO;
import co.edu.uniquindio.application.dtos.reserva.CreacionReservaDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.exceptions.NoFoundException;
import co.edu.uniquindio.application.exceptions.ValidationException;
import co.edu.uniquindio.application.mappers.AlojamientoMapper;
import co.edu.uniquindio.application.mappers.ReservaMapper;
import co.edu.uniquindio.application.mappers.UsuarioMapper;
import co.edu.uniquindio.application.models.entitys.Alojamiento;
import co.edu.uniquindio.application.models.entitys.Reserva;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.models.enums.Estado;
import co.edu.uniquindio.application.models.enums.ReservaEstado;
import co.edu.uniquindio.application.repositories.AlojamientoRepositorio;
import co.edu.uniquindio.application.repositories.ReservaRepositorio;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.services.EmailServicio;
import co.edu.uniquindio.application.services.ReservaServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservaServicioImpl implements ReservaServicio {

    private final ReservaRepositorio reservaRepositorio;
    private final AlojamientoRepositorio alojamientoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final ReservaMapper reservaMapper;
    private final AlojamientoMapper alojamientoMapper;
    private final UsuarioMapper usuarioMapper;
    private final EmailServicio emailServicio;

    @Override
    public void crear(CreacionReservaDTO dto) throws Exception {

        // 1. Obtener usuario autenticado
        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();

        // Verificar que el usuario autenticado sea el mismo que está haciendo la reserva
        if (!idUsuarioAutenticado.equals(dto.usuarioId())) {
            throw new AccessDeniedException("No puedes hacer reservas en nombre de otro usuario");
        }

        // 2. Obtener y validar usuario
        Usuario huesped = usuarioRepositorio.findById(String.valueOf(dto.usuarioId()))
                .orElseThrow(() -> new NoFoundException("Usuario no encontrado"));

        if (huesped.getEstado() == Estado.ELIMINADO) {
            throw new ValidationException("El usuario está inactivo");
        }

        // 3. Obtener y validar alojamiento
        Alojamiento alojamiento = alojamientoRepositorio.findById(dto.alojamientoId())
                .orElseThrow(() -> new NoFoundException("Alojamiento no encontrado"));

        if (alojamiento.getEstado() == Estado.ELIMINADO) {
            throw new ValidationException("El alojamiento no está disponible");
        }

        // 4. Validar que el usuario no sea el anfitrión del alojamiento
        if (alojamiento.getAnfitrion().getId().equals(idUsuarioAutenticado)) {
            throw new ValidationException("No puedes reservar tu propio alojamiento");
        }

        // 5. Validar fechas
        validarFechas(dto.fechaEntrada(), dto.fechaSalida());

        // 6. Validar capacidad
        if (dto.cantidadHuespedes() > alojamiento.getMaxHuespedes()) {
            throw new ValidationException(
                    "El número de huéspedes (" + dto.cantidadHuespedes() +
                            ") supera la capacidad máxima del alojamiento (" + alojamiento.getMaxHuespedes() + ")"
            );
        }

        // 7. Validar disponibilidad (no hay solapamiento con otras reservas)
        if (existeSolapamiento(alojamiento.getId(), dto.fechaEntrada(), dto.fechaSalida())) {
            throw new ValidationException("El alojamiento no está disponible en las fechas seleccionadas");
        }

        // 8. Calcular precio total
        long numeroNoches = ChronoUnit.DAYS.between(dto.fechaEntrada(), dto.fechaSalida());
        double precioTotal = numeroNoches * alojamiento.getPrecioPorNoche();

        // 9. Crear reserva
        Reserva reserva = reservaMapper.toEntity(dto);
        reserva.setPrecio(precioTotal);
        reserva.setAlojamiento(alojamiento);
        reserva.setHuesped(huesped);
        reserva = reservaRepositorio.save(reserva);

        // 10. Enviar emails de confirmación
        enviarEmailsConfirmacion(reserva, alojamiento, huesped);
    }

    @Override
    public List<ItemReservaDTO> listarReservas(Long id, String estado, String fechaInicio, String fechaFin, int pagina) throws Exception {
        return null;
    }

    @Override
    public void cancelarReserva(Long id) throws Exception {

        // Obtener usuario autenticado
        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();

        // Obtener reserva
        Reserva reserva = reservaRepositorio.findById(id)
                .orElseThrow(() -> new NoFoundException("Reserva no encontrada"));

        // Verificar que el usuario sea el dueño de la reserva
        if (!reserva.getHuesped().getId().equals(idUsuarioAutenticado)) {
            throw new AccessDeniedException("No tienes permiso para cancelar esta reserva");
        }

        // Validar que la reserva esté en estado CONFIRMADA o PENDIENTE
        if (reserva.getEstado() != ReservaEstado.CONFIRMADA &&
                reserva.getEstado() != ReservaEstado.PENDIENTE) {
            throw new ValidationException("Solo se pueden cancelar reservas confirmadas o pendientes");
        }

        // Validar que falten al menos 48 horas para el check-in
        LocalDateTime fechaLimite = reserva.getFechaEntrada().atStartOfDay().minusHours(48);
        if (LocalDateTime.now().isAfter(fechaLimite)) {
            throw new ValidationException(
                    "No se puede cancelar la reserva. Debe hacerlo con al menos 48 horas de anticipación"
            );
        }

        // Cambiar estado a CANCELADA
        reserva.setEstado(ReservaEstado.CANCELADA);
        reservaRepositorio.save(reserva);

        // Enviar emails de notificación
        enviarEmailsCancelacion(reserva);
    }

    /**
     * Valida que las fechas sean coherentes
     */
    private void validarFechas(LocalDate fechaEntrada, LocalDate fechaSalida) throws ValidationException {
        LocalDate hoy = LocalDate.now();

        // No se pueden reservar fechas pasadas
        if (fechaEntrada.isBefore(hoy)) {
            throw new ValidationException("No se pueden reservar fechas pasadas");
        }

        // La fecha de salida debe ser posterior a la de entrada
        if (fechaSalida.isBefore(fechaEntrada) || fechaSalida.isEqual(fechaEntrada)) {
            throw new ValidationException("La fecha de salida debe ser posterior a la fecha de entrada");
        }

        // Mínimo 1 noche
        long noches = ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
        if (noches < 1) {
            throw new ValidationException("La reserva debe ser de al menos 1 noche");
        }
    }

    /**
     * Verifica si hay solapamiento con otras reservas confirmadas o pendientes
     */
    private boolean existeSolapamiento(Long alojamientoId, LocalDate fechaEntrada,
                                       LocalDate fechaSalida) {

        List<Reserva> reservasExistentes = reservaRepositorio
                .findByAlojamiento_IdAndEstadoIn(
                        alojamientoId,
                        List.of(ReservaEstado.CONFIRMADA, ReservaEstado.PENDIENTE)
                );

        for (Reserva reserva : reservasExistentes) {

            // Verificar solapamiento
            boolean haySolapamiento = !(fechaSalida.isBefore(reserva.getFechaEntrada()) ||
                    fechaEntrada.isAfter(reserva.getFechaEntrada()));

            if (haySolapamiento) {
                return true;
            }
        }

        return false;
    }

    /**
     * Envía emails de confirmación al huésped y al anfitrión
     */
    private void enviarEmailsConfirmacion(Reserva reserva, Alojamiento alojamiento, Usuario huesped) {
        // Email al huésped
        String asuntoHuesped = "Reserva confirmada - " + alojamiento.getTitulo();
        String cuerpoHuesped = String.format(
                "¡Hola %s!\n\n" +
                        "Tu reserva ha sido confirmada.\n\n" +
                        "Detalles:\n" +
                        "- Alojamiento: %s\n" +
                        "- Check-in: %s\n" +
                        "- Check-out: %s\n" +
                        "- Huéspedes: %d\n" +
                        "- Precio total: $%.2f\n\n" +
                        "¡Esperamos que disfrutes tu estadía!",
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
            // Log error pero no fallar la transacción
            System.err.println("Error enviando email al huésped: " + e.getMessage());
        }

        // Email al anfitrión
        String asuntoAnfitrion = "Nueva reserva en tu alojamiento - " + alojamiento.getTitulo();
        String cuerpoAnfitrion = String.format(
                "¡Hola %s!\n\n" +
                        "Tienes una nueva reserva.\n\n" +
                        "Detalles:\n" +
                        "- Alojamiento: %s\n" +
                        "- Huésped: %s\n" +
                        "- Check-in: %s\n" +
                        "- Check-out: %s\n" +
                        "- Número de huéspedes: %d\n" +
                        "- Precio total: $%.2f",
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

    /**
     * Envía emails de cancelación al huésped y al anfitrión
     */
    private void enviarEmailsCancelacion(Reserva reserva) {
        // Email al huésped
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

        // Email al anfitrión
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