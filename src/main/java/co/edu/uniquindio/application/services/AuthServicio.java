package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.usuario.*;

public interface AuthServicio {
    //UsuarioDTO registro(CreacionAnfitrionDTO anfitrionDTO) throws Exception;
    TokenDTO login(LoginDTO loginDTO) throws Exception;
    //void solicitarRecuperacion(OlvidoContrasenaDTO olvidoContrasenaDTO) throws Exception;
    //void restablecerContrasena(ReinicioContrasenaDTO reinicioContrasenaDTO) throws Exception;
}
