package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.EmailDTO;
import co.edu.uniquindio.application.dtos.resena.CreacionResenaDTO;
import co.edu.uniquindio.application.dtos.resena.CreacionRespuestaDTO;
import co.edu.uniquindio.application.dtos.resena.ItemResenaDTO;
import co.edu.uniquindio.application.exceptions.NoFoundException;
import co.edu.uniquindio.application.exceptions.ValidationException;
import co.edu.uniquindio.application.exceptions.ValueConflictException;
import co.edu.uniquindio.application.mappers.ResenaMapper;
import co.edu.uniquindio.application.models.entitys.Alojamiento;
import co.edu.uniquindio.application.models.entitys.Resena;
import co.edu.uniquindio.application.models.entitys.Reserva;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.models.enums.Estado;
import co.edu.uniquindio.application.models.enums.ReservaEstado;
import co.edu.uniquindio.application.models.vo.Respuesta;
import co.edu.uniquindio.application.repositories.AlojamientoRepositorio;
import co.edu.uniquindio.application.repositories.ResenaRepositorio;
import co.edu.uniquindio.application.repositories.ReservaRepositorio;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.services.EmailServicio;
import co.edu.uniquindio.application.services.ResenaServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResenaServicioImpl implements ResenaServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final ResenaRepositorio resenaRepositorio;
    private final ResenaMapper resenaMapper;
    private final EmailServicio emailServicio;
    private final AlojamientoRepositorio alojamientoRepositorio;

    @Override
    public void crear(Long idAlojamiento, CreacionResenaDTO dto) throws Exception{

        // 1. Obtener usuario autenticado
        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();

        Usuario usuario = usuarioRepositorio.findById(idUsuarioAutenticado)
                .orElseThrow(() -> new NoFoundException("Usuario no encontrado"));


        // 2. Obtener alojamiento
        Alojamiento alojamiento = alojamientoRepositorio.findById(idAlojamiento)
                .orElseThrow(() -> new NoFoundException("Alojamiento no encontrado"));

        if (alojamiento.getEstado() == Estado.ELIMINADO) {
            throw new ValidationException("El alojamiento no está disponible");
        }

        // 2. Obtener alojamiento
        Reserva reserva = reservaRepositorio.findByHuesped_IdAndAlojamiento_IdAndEstadoIn(idUsuarioAutenticado, idAlojamiento, List.of(ReservaEstado.CONFIRMADA));

        // 3. Validar que el usuario ya se hubiera quedado en el alojamiento
        if (reserva == null) {
            throw new AccessDeniedException("Solo puedes reseñar alojamientos donde hayas sido huésped y este completada la reserva");
        }


        // 5. Validar que no exista ya una reseña para esta reserva
        boolean yaReseno = resenaRepositorio.existsByUsuario_IdAndAlojamiento_Id(
                idUsuarioAutenticado,
                reserva.getAlojamiento().getId()
        );

        if (yaReseno) {
            throw new ValueConflictException("Ya has reseñado este alojamiento");
        }

        // 7. Crear reseña
        Resena resena = resenaMapper.toEntity(dto);
        resena.setAlojamiento(alojamiento);
        resena.setUsuario(usuario);
        resenaRepositorio.save(resena);

        // 8. Actualizar promedio de calificaciones del alojamiento
        actualizarPromedioCalificaciones(alojamiento.getId());

        // 9. Notificar al anfitrión
        enviarEmailNuevaResena(resena, alojamiento);
    }

    @Override
    public void responder(Long resenaId, CreacionRespuestaDTO dto) throws Exception{

        // 1. Obtener usuario autenticado
        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();

        // 2. Obtener reseña
        Resena resena = resenaRepositorio.findById(resenaId)
                .orElseThrow(() -> new NoFoundException("Reseña no encontrada"));

        // 3. Validar que el usuario autenticado sea el anfitrión del alojamiento
        if (!resena.getAlojamiento().getAnfitrion().getId().equals(idUsuarioAutenticado)) {
            throw new AccessDeniedException("Solo el anfitrión del alojamiento puede responder reseñas");
        }

        // 4. Validar que no haya ya una respuesta
        if (resena.getRespuesta() != null) {
            throw new ValueConflictException("Ya has respondido a esta reseña");
        }

        // 5. Crear y guardar respuesta
        Respuesta respuesta = new Respuesta(dto.mensaje(), LocalDateTime.now());
        resena.setRespuesta(respuesta);
        resenaRepositorio.save(resena);

        // 6. Notificar al huésped
        enviarEmailRespuestaResena(resena);
    }

    @Override
    public List<ItemResenaDTO> obtenerResenasAlojamiento(Long alojamientoId, int pagina) throws Exception{

        // Validar que el alojamiento existe
        if (!alojamientoRepositorio.existsById(alojamientoId)) {
            throw new NoFoundException("Alojamiento no encontrado");
        }

        Pageable pageable = PageRequest.of(pagina, 5);
        Page<ItemResenaDTO> resenas = resenaRepositorio.findByAlojamiento_IdOrderByCreadoEnDesc(alojamientoId, pageable).map(resenaMapper::toItemDTO);

        return resenas.toList();
    }

    /**
     * Actualiza el promedio de calificaciones de un alojamiento
     */
    private void actualizarPromedioCalificaciones(Long alojamientoId) {
        Alojamiento alojamiento = alojamientoRepositorio.findById(alojamientoId)
                .orElseThrow(() -> new RuntimeException("Alojamiento no encontrado"));

        Double promedio = resenaRepositorio.calcularPromedioCalificaciones(alojamientoId);
        Integer total = resenaRepositorio.contarResenas(alojamientoId);

        alojamiento.setPromedioCalificaciones(promedio != null ? promedio : 0.0);
        alojamiento.setNumeroCalificaciones(total);

        alojamientoRepositorio.save(alojamiento);
    }

    /**
     * Envía email al anfitrión cuando recibe una nueva reseña
     */
    private void enviarEmailNuevaResena(Resena resena, Alojamiento alojamiento) {
        String asunto = "Nueva reseña en tu alojamiento - " + alojamiento.getTitulo();
        String cuerpo = String.format(
                "Hola %s,\n\n" +
                        "Has recibido una nueva reseña en tu alojamiento '%s'.\n\n" +
                        "Calificación: %.1f/5 estrellas\n" +
                        "Comentario: %s\n" +
                        "De: %s\n\n" +
                        "Puedes responder a esta reseña desde tu panel de anfitrión.",
                alojamiento.getAnfitrion().getNombre(),
                alojamiento.getTitulo(),
                resena.getCalificacion(),
                resena.getComentario(),
                resena.getUsuario().getNombre()
        );

        try {
            emailServicio.enviarEmail(new EmailDTO(
                    asunto,
                    cuerpo,
                    alojamiento.getAnfitrion().getEmail()
            ));
        } catch (Exception e) {
            System.err.println("Error enviando email de nueva reseña: " + e.getMessage());
        }
    }

    /**
     * Envía email al huésped cuando el anfitrión responde su reseña
     */
    private void enviarEmailRespuestaResena(Resena resena) {
        String asunto = "El anfitrión respondió tu reseña - " + resena.getAlojamiento().getTitulo();
        String cuerpo = String.format(
                "Hola %s,\n\n" +
                        "%s ha respondido a tu reseña sobre '%s'.\n\n" +
                        "Respuesta: %s\n\n" +
                        "¡Gracias por compartir tu experiencia!",
                resena.getUsuario().getNombre(),
                resena.getAlojamiento().getAnfitrion().getNombre(),
                resena.getAlojamiento().getTitulo(),
                resena.getRespuesta().getMensaje()
        );

        try {
            emailServicio.enviarEmail(new EmailDTO(
                    asunto,
                    cuerpo,
                    resena.getUsuario().getEmail()
            ));
        } catch (Exception e) {
            System.err.println("Error enviando email de respuesta a reseña: " + e.getMessage());
        }
    }
}
