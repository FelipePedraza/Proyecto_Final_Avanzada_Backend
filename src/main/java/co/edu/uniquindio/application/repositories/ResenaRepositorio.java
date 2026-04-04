package co.edu.uniquindio.application.repositories;

import co.edu.uniquindio.application.models.entitys.Resena;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

@Repository
public interface ResenaRepositorio  extends JpaRepository<Resena, Long> {

    /**
     * Verifica si un usuario ya reseñó un alojamiento
     */
    boolean existsByUsuario_IdAndAlojamiento_Id(String usuarioId, Long alojamientoId);

    /**
     * Calcula el promedio de calificaciones de un alojamiento
     */
    @Query("SELECT AVG(r.calificacion) FROM Resena r WHERE r.alojamiento.id = :alojamientoId")
    Double calcularPromedioCalificaciones(@Param("alojamientoId") Long alojamientoId);

    /**
     * Cuenta el total de reseñas de un alojamiento
     */
    @Query("SELECT COUNT(r) FROM Resena r WHERE r.alojamiento.id = :alojamientoId")
    Integer contarResenas(@Param("alojamientoId") Long alojamientoId);

    /**
     * Lista reseñas de un alojamiento (ordenamiento via Pageable Sort)
     * NOTA: Usa @Query explícito sin ORDER BY para evitar duplicación en SQL Server
     */
    @Query("SELECT r FROM Resena r WHERE r.alojamiento.id = :alojamientoId")
    Page<Resena> buscarPorAlojamientoId(@Param("alojamientoId") Long alojamientoId, Pageable pageable);
}
