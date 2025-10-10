package co.edu.uniquindio.application.repositories;

import co.edu.uniquindio.application.models.entitys.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepositorio extends JpaRepository<Chat, Long> {

    /**
     * Busca un chat entre dos usuarios específicos
     */
    @Query("""
        SELECT c FROM Chat c 
        WHERE ((c.usuario1.id = :usuario1Id AND c.usuario2.id = :usuario2Id) 
        OR (c.usuario1.id = :usuario2Id AND c.usuario2.id = :usuario1Id))
        AND c.activo = true
        """)
    Optional<Chat> findChatEntreUsuarios(@Param("usuario1Id") String usuario1Id, @Param("usuario2Id") String usuario2Id);

    /**
     * Lista todos los chats de un usuario específico
     */
    @Query("""
        SELECT c FROM Chat c 
        WHERE (c.usuario1.id = :usuarioId OR c.usuario2.id = :usuarioId)
        AND c.activo = true
        ORDER BY c.creadoEn DESC
        """)
    List<Chat> findChatsByUsuario(@Param("usuarioId") String usuarioId);

    /**
     * Busca chats con paginación para un usuario específico
     */
    @Query("""
        SELECT c FROM Chat c 
        WHERE (c.usuario1.id = :usuarioId OR c.usuario2.id = :usuarioId)
        AND c.activo = true
        ORDER BY c.creadoEn DESC
        """)
    Page<Chat> findChatsByUsuario(@Param("usuarioId") String usuarioId, Pageable pageable);

    /**
     * Verifica si un usuario es participante de un chat específico
     */
    @Query("""
        SELECT COUNT(c) > 0 FROM Chat c 
        WHERE c.id = :chatId 
        AND (c.usuario1.id = :usuarioId OR c.usuario2.id = :usuarioId)
        """)
    boolean esParticipanteDelChat(@Param("chatId") Long chatId, @Param("usuarioId") String usuarioId);

    /**
     * Obtiene el otro participante del chat (no el usuario actual)
     */
    @Query("""
        SELECT CASE 
            WHEN c.usuario1.id = :usuarioId THEN c.usuario2
            ELSE c.usuario1
        END FROM Chat c 
        WHERE c.id = :chatId
        """)
    Optional<co.edu.uniquindio.application.models.entitys.Usuario> getOtroParticipante(@Param("chatId") Long chatId, @Param("usuarioId") String usuarioId);
}
