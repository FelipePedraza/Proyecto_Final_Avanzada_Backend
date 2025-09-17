package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.usuario.LoginDTO;
import co.edu.uniquindio.application.dtos.usuario.LoginResponseDTO;
import co.edu.uniquindio.application.dtos.usuario.CreacionAnfitrionDTO;
import co.edu.uniquindio.application.dtos.usuario.UsuarioDTO;
import co.edu.uniquindio.application.dtos.usuario.OlvidoContrasenaDTO;
import co.edu.uniquindio.application.dtos.usuario.ReiniciarContrasena;

public interface AuthServicio {
    UsuarioDTO registro(CreacionAnfitrionDTO anfitrionDTO) throws Exception;
    LoginResponseDTO login(LoginDTO loginDTO) throws Exception;
    void solicitarRecuperacion(OlvidoContrasenaDTO olvidoContrasenaDTO) throws Exception;
    void restablecerContrasena(ReiniciarContrasena reiniciarContrasena) throws Exception;
}