package co.edu.uniquindio.application.repositories;

import co.edu.uniquindio.application.models.entitys.Alojamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlojamientoRepositorio extends JpaRepository<Alojamiento, Long> {
}
