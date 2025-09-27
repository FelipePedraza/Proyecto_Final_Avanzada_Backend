package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.alojamiento.ItemAlojamientoDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.usuario.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UsuarioServicio {
    void crear(CreacionUsuarioDTO dto) throws Exception;
    void editar(String id,EdicionUsuarioDTO dto) throws Exception;
    void eliminar(String id) throws Exception;
    UsuarioDTO obtener(String id) throws Exception;
    void cambiarContrasena(String id, CambioContrasenaDTO dto) throws Exception;
    void reiniciarContrasena(ReinicioContrasenaDTO dto) throws Exception;
    void crearAnfitrion(String usuarioId, CreacionAnfitrionDTO dto) throws Exception;
}
