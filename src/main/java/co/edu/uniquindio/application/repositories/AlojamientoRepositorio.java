package co.edu.uniquindio.application.repositories;

import co.edu.uniquindio.application.models.entitys.Alojamiento;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import co.edu.uniquindio.application.models.enums.Estado;
import co.edu.uniquindio.application.models.enums.Servicio;
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
    Page<Alojamiento> getAlojamientos(String idUsuario, Estado estado, Pageable pageable);

    @Query("""
    SELECT DISTINCT a FROM Alojamiento a
    LEFT JOIN a.reservas r
    WHERE a.estado = :estado
    AND (:ciudad IS NULL OR LOWER(a.direccion.ciudad) LIKE LOWER(CONCAT('%', :ciudad, '%')))
    AND (:huespedes IS NULL OR a.maxHuespedes >= :huespedes)
    AND (:precioMin IS NULL OR a.precioPorNoche >= :precioMin)
    AND (:precioMax IS NULL OR a.precioPorNoche <= :precioMax)
    AND (
        :servicios IS NULL OR :servicios IS EMPTY OR
        (SELECT COUNT(DISTINCT s) FROM a.servicios s WHERE s IN :servicios) = :cantidadServicios
    )
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
            @Param("servicios") List<Servicio> servicios,  // Enum type
            @Param("cantidadServicios") Long cantidadServicios,
            @Param("estado") Estado estado,
            Pageable pageable
    );

    /**
     * Busca alojamientos por ciudad y los ordena por calificación descendente
     * Para sugerencias basadas en la ciudad
     */
    @Query("""
    SELECT a FROM Alojamiento a 
    WHERE a.estado = :estado 
    AND LOWER(a.direccion.ciudad) LIKE LOWER(CONCAT('%', :ciudad, '%'))
    ORDER BY a.promedioCalificaciones DESC NULLS LAST
    """)
    Page<Alojamiento> sugerirPorCiudad(
            @Param("ciudad") String ciudad,
            @Param("estado") Estado estado,
            Pageable pageable
    );
}
