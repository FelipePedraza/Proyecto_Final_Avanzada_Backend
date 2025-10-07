package co.edu.uniquindio.application.repositories;

import co.edu.uniquindio.application.models.entitys.Reserva;
import co.edu.uniquindio.application.models.enums.ReservaEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepositorio extends JpaRepository<Reserva, Long> {

    /**
     * Busca reservas de un alojamiento en estados específicos
     */
    List<Reserva> findByAlojamiento_IdAndEstadoIn(Long alojamientoId, List<ReservaEstado> estados);

    /**
     * Verifica si existe una reserva entre un huésped y un anfitrión
     */
    boolean existsByHuesped_IdAndAlojamiento_Anfitrion_Id(String huespedId, String anfitrionId);

    /**
     * Busca reservas completadas de un usuario en un alojamiento específico
     */
    @Query("""
        SELECT r FROM Reserva r
        WHERE r.huesped.id = :huespedId
        AND r.alojamiento.id = :alojamientoId
        AND r.estado = 'COMPLETADA'
        AND r.fechaSalida < :fechaActual
    """)
    List<Reserva> findReservasCompletadas(
            @Param("huespedId") String huespedId,
            @Param("alojamientoId") Long alojamientoId,
            @Param("fechaActual") LocalDate fechaActual
    );

    /**
     * Cuenta reservas futuras de un alojamiento
     */
    @Query("""
        SELECT COUNT(r) FROM Reserva r
        WHERE r.alojamiento.id = :alojamientoId
        AND r.estado IN ('CONFIRMADA', 'PENDIENTE')
        AND r.fechaEntrada > :fechaActual
    """)
    long countReservasFuturas(
            @Param("alojamientoId") Long alojamientoId,
            @Param("fechaActual") LocalDate fechaActual
    );
}
