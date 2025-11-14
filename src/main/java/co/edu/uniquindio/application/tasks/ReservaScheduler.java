
package co.edu.uniquindio.application.tasks;

import co.edu.uniquindio.application.dtos.EmailDTO;
import co.edu.uniquindio.application.models.entitys.Reserva;
import co.edu.uniquindio.application.models.enums.ReservaEstado;
import co.edu.uniquindio.application.repositories.ReservaRepositorio;
import co.edu.uniquindio.application.services.EmailServicio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Tarea programada para actualizar automáticamente el estado de las reservas
 * y enviar emails de invitación a reseñar
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservaScheduler {

    private final ReservaRepositorio reservaRepositorio;
    private final EmailServicio emailServicio;

    /**
     * Se ejecuta todos los días a las 6
     * Actualiza las reservas confirmadas cuya fecha de salida ya pasó a COMPLETADA
     * y envía un email al huésped invitándolo a dejar una reseña
     */
    @Scheduled(cron = "0 00 00 * * *")  // Todos los días a las 12:00 AM
    @Transactional
    public void actualizarReservasCompletadas() {
        log.info("Iniciando tarea de actualización de reservas completadas...");

        LocalDate hoy = LocalDate.now();

        // Buscar reservas confirmadas cuya fecha de salida ya pasó
        List<Reserva> reservasACompletar = reservaRepositorio.findAll()
                .stream()
                .filter(r -> r.getEstado() == ReservaEstado.CONFIRMADA)
                .filter(r -> r.getFechaSalida().isBefore(hoy))
                .toList();

        int actualizadas = 0;
        for (Reserva reserva : reservasACompletar) {
            reserva.setEstado(ReservaEstado.COMPLETADA);
            reservaRepositorio.save(reserva);

            // Enviar email al huésped invitándolo a dejar reseña
            enviarEmailReservaCompletada(reserva);

            actualizadas++;
        }

        log.info("Tarea finalizada. {} reservas actualizadas a COMPLETADA", actualizadas);
    }

    /**
     * Envía email al huésped cuando la reserva se completa, invitándolo a dejar reseña
     */
    private void enviarEmailReservaCompletada(Reserva reserva) {
        String asunto = "¡Esperamos que hayas disfrutado tu estadía! - " + reserva.getAlojamiento().getTitulo();
        String cuerpo = String.format(
                "Hola %s,\n\n" +
                        "Tu estadía en '%s' ha finalizado.\n\n" +
                        "Esperamos que hayas tenido una experiencia maravillosa. " +
                        "¿Te gustaría compartir tu experiencia dejando una reseña?\n\n" +
                        "Tu opinión es muy valiosa para otros viajeros y para nuestros anfitriones.\n\n" +
                        "¡Gracias por usar ViviGo!",
                reserva.getHuesped().getNombre(),
                reserva.getAlojamiento().getTitulo()
        );

        try {
            emailServicio.enviarEmail(new EmailDTO(asunto, cuerpo, reserva.getHuesped().getEmail()));
        } catch (Exception e) {
            System.err.println("Error enviando email de reserva completada: " + e.getMessage());
        }
    }
}

