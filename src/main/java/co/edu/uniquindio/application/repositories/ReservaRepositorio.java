package co.edu.uniquindio.application.repositories;

import co.edu.uniquindio.application.models.entitys.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaRepositorio extends JpaRepository<Reserva, Long> {
}
