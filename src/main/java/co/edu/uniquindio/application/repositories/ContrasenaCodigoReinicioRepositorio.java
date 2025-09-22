package co.edu.uniquindio.application.repositories;

import co.edu.uniquindio.application.models.entitys.ContrasenaCodigoReinicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContrasenaCodigoReinicioRepositorio extends JpaRepository<ContrasenaCodigoReinicio, Long> {
   Optional<ContrasenaCodigoReinicio> findByUsuario_Email(String email);
}
