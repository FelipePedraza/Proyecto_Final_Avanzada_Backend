package co.edu.uniquindio.application.repositories;

import co.edu.uniquindio.application.models.entitys.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MensajeRepositorio extends JpaRepository<Mensaje, Long> {

    /**
     * Lista mensajes de un chat específico ordenados por fecha de envío
     */
    Page<Mensaje> findByChat_IdOrderByFechaEnvioAsc(Long chatId, Pageable pageable);

    /**
     * Lista mensajes de un chat específico ordenados por fecha de envío descendente
     */
    List<Mensaje> findByChat_IdOrderByFechaEnvioDesc(Long chatId);

    /**
     * Obtiene el último mensaje de un chat
     */
    @Query("""
        SELECT m FROM Mensaje m 
        WHERE m.chat.id = :chatId 
        ORDER BY m.fechaEnvio DESC 
        LIMIT 1
        """)
    Optional<Mensaje> findUltimoMensajeDelChat(@Param("chatId") Long chatId);

    /**
     * Cuenta mensajes no leídos de un chat para un usuario específico
     */
    @Query("""
        SELECT COUNT(m) FROM Mensaje m 
        WHERE m.chat.id = :chatId 
        AND m.destinatario.id = :usuarioId 
        AND m.leido = false
        """)
    Long contarMensajesNoLeidos(@Param("chatId") Long chatId, @Param("usuarioId") String usuarioId);

    /**
     * Marca mensajes como leídos para un usuario específico en un chat
     */
    @Modifying
    @Query("""
        UPDATE Mensaje m 
        SET m.leido = true 
        WHERE m.chat.id = :chatId 
        AND m.destinatario.id = :usuarioId 
        AND m.leido = false
        """)
    void marcarMensajesComoLeidos(@Param("chatId") Long chatId, @Param("usuarioId") String usuarioId);

    /**
     * Lista mensajes no leídos de un usuario en todos sus chats
     */
    @Query("""
        SELECT m FROM Mensaje m 
        WHERE m.destinatario.id = :usuarioId 
        AND m.leido = false 
        ORDER BY m.fechaEnvio DESC
        """)
    List<Mensaje> findMensajesNoLeidosPorUsuario(@Param("usuarioId") String usuarioId);

    /**
     * Verifica si un usuario puede ver un mensaje específico
     */
    @Query("""
        SELECT COUNT(m) > 0 FROM Mensaje m 
        JOIN m.chat c 
        WHERE m.id = :mensajeId 
        AND (c.usuario1.id = :usuarioId OR c.usuario2.id = :usuarioId)
        """)
    boolean puedeVerMensaje(@Param("mensajeId") Long mensajeId, @Param("usuarioId") String usuarioId);
}
