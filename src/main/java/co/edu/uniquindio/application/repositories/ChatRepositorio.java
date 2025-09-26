package co.edu.uniquindio.application.repositories;

import co.edu.uniquindio.application.models.entitys.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepositorio extends JpaRepository<Chat, Long> {
    List<Chat> findByReservaId(Long reservaId);
    List<Chat> findByParticipantes_Id(String usuarioId);
}
