package co.edu.uniquindio.application.repositories;

import co.edu.uniquindio.application.models.entitys.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByEmail(String email);
}
