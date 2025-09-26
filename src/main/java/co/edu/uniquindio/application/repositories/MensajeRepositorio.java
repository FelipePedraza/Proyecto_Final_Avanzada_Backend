package co.edu.uniquindio.application.repositories;

import co.edu.uniquindio.application.models.entitys.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepositorio extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByChatIdOrderByFechaEnvioAsc(Long chatId);
    List<Mensaje> findByDestinatario_IdAndLeidoFalse(String destinatarioId);
}
