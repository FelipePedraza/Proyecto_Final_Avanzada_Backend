package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.usuario.*;
import co.edu.uniquindio.application.services.AuthServicio;
import org.springframework.stereotype.Service;

@Service
public class AuthServicioImpl implements AuthServicio {

    @Override
    public UsuarioDTO registro(CreacionAnfitrionDTO anfitrionDTO) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public LoginResponseDTO login(LoginDTO loginDTO) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public void solicitarRecuperacion(OlvidoContrasenaDTO olvidoContrasenaDTO) throws Exception {
        // Lógica de negocio a implementar
    }

    @Override
    public void restablecerContrasena(ReiniciarContrasena reiniciarContrasena) throws Exception {
        // Lógica de negocio a implementar
    }
}
