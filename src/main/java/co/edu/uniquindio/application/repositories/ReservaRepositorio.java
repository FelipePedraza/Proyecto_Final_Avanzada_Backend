package co.edu.uniquindio.application.repositories;

import co.edu.uniquindio.application.models.entitys.Reserva;
import co.edu.uniquindio.application.models.enums.ReservaEstado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("SELECT r FROM Reserva r WHERE r.huesped.id = :idUsuario "
            + "AND (:estado IS NULL OR r.estado = :estado) "
            + "AND (:fechaEntrada  IS NULL OR r.fechaEntrada >= :fechaEntrada ) "
            + "AND (:fechaSalida IS NULL OR r.fechaEntrada  <= :fechaSalida )")
    Page<Reserva> buscarConFiltros(
            String idUsuario,
            @Param("estado") ReservaEstado estado,
            @Param("fechaEntrada") LocalDate fechaEntrada,
            @Param("fechaSalida") LocalDate fechaSalida ,
            Pageable pageable
    );
}
