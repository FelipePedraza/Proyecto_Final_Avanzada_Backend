package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.Usuarios.CreacionUsuarioDTO;
import co.edu.uniquindio.application.dtos.Usuarios.EdicionUsuarioDTO;
import co.edu.uniquindio.application.dtos.Usuarios.UsuarioDTO;
import co.edu.uniquindio.application.dtos.Usuarios.CambioContrasenaDTO;


public interface UsuarioServicio {
    UsuarioDTO crear(CreacionUsuarioDTO dto) throws Exception;
    UsuarioDTO editar(EdicionUsuarioDTO dto) throws Exception;
    void eliminar(Long id) throws Exception;
    UsuarioDTO obtener(Long id) throws Exception;
    void cambiarContrasena(Long id, CambioContrasenaDTO dto) throws Exception;
}