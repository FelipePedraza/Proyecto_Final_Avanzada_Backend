package co.edu.uniquindio.application.repositories;

import co.edu.uniquindio.application.models.entitys.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResenaRepositorio  extends JpaRepository<Resena, Long> {
	List<Resena> findByAlojamientoId(Long alojamientoId);
}
