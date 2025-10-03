package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.usuario.*;
import org.springframework.web.multipart.MultipartFile;


public interface UsuarioServicio {
    void crear(CreacionUsuarioDTO dto) throws Exception;
    void editar(String id, EdicionUsuarioDTO dto, MultipartFile file) throws Exception;
    void eliminar(String id) throws Exception;
    UsuarioDTO obtener(String id) throws Exception;
    void cambiarContrasena(String id, CambioContrasenaDTO dto) throws Exception;
    void reiniciarContrasena(ReinicioContrasenaDTO dto) throws Exception;
    void crearAnfitrion(CreacionAnfitrionDTO dto) throws Exception;
}
