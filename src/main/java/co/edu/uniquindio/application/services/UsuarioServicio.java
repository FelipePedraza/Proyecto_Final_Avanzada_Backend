package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.usuario.*;



public interface UsuarioServicio {
    void crear(CreacionUsuarioDTO dto) throws Exception;
    void editar(String id, EdicionUsuarioDTO dto) throws Exception;
    void eliminar(String id) throws Exception;
    UsuarioDTO obtener(String id) throws Exception;
    void cambiarContrasena(String id, CambioContrasenaDTO dto) throws Exception;
    void crearAnfitrion(CreacionAnfitrionDTO dto) throws Exception;
}
