package co.edu.uniquindio.application.repositories;

import co.edu.uniquindio.application.dtos.alojamiento.ItemAlojamientoDTO;
import co.edu.uniquindio.application.models.entitys.Alojamiento;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface AlojamientoRepositorio extends JpaRepository<Alojamiento, Long> {

    Optional<Alojamiento> findByTitulo(String titulo);

    @Query("select a from Alojamiento a where a.anfitrion.id = :idUsuario")
    Page<Alojamiento> getAlojamientos(String idUsuario, Pageable pageable);
}
