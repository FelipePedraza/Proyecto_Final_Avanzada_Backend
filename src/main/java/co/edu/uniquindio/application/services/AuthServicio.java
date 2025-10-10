package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.usuario.*;

public interface AuthServicio {
    TokenDTO login(LoginDTO loginDTO) throws Exception;
    Boolean obtnerIdAutenticado(String idUsuario); 
    //void solicitarRecuperacion(OlvidoContrasenaDTO olvidoContrasenaDTO) throws Exception;
    void reiniciarContrasena(ReinicioContrasenaDTO dto) throws Exception;
}
