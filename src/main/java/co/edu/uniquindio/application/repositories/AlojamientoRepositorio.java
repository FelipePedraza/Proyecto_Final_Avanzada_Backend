package co.edu.uniquindio.application.repositories;

import co.edu.uniquindio.application.models.entitys.Alojamiento;

import java.time.LocalDate;
import java.util.Optional;

import co.edu.uniquindio.application.models.enums.Estado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface AlojamientoRepositorio extends JpaRepository<Alojamiento, Long> {

    Optional<Alojamiento> findByTitulo(String titulo);

    @Query("select a from Alojamiento a where a.anfitrion.id = :idUsuario and a.estado = :estado")
    Page<Alojamiento> getAlojamientos(String idUsuario, Estado estado,Pageable pageable);

    @Query("""
        SELECT DISTINCT a FROM Alojamiento a
        LEFT JOIN a.reservas r
        WHERE a.estado = :estado
        AND (:ciudad IS NULL OR LOWER(a.direccion.ciudad) LIKE LOWER(CONCAT('%', :ciudad, '%')))
        AND (:huespedes IS NULL OR a.maxHuespedes >= :huespedes)
        AND (:precioMin IS NULL OR a.precioPorNoche >= :precioMin)
        AND (:precioMax IS NULL OR a.precioPorNoche <= :precioMax)
        AND (
            :fechaEntrada IS NULL OR :fechaSalida IS NULL OR
            a.id NOT IN (
                SELECT r2.alojamiento.id FROM Reserva r2
                WHERE r2.estado IN ('CONFIRMADA', 'PENDIENTE')
                AND (
                    (r2.fechaEntrada <= :fechaSalida AND r2.fechaSalida >= :fechaEntrada)
                )
            )
        )
        ORDER BY a.creadoEn DESC
    """)
    Page<Alojamiento> buscarConFiltros(
            @Param("ciudad") String ciudad,
            @Param("fechaEntrada") LocalDate fechaEntrada,
            @Param("fechaSalida") LocalDate fechaSalida,
            @Param("huespedes") Integer huespedes,
            @Param("precioMin") Float precioMin,
            @Param("precioMax") Float precioMax,
            @Param("estado") Estado estado,
            Pageable pageable
    );
}
