package co.edu.uniquindio.application.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UsuarioDetallesServicio {
    UserDetails cargarUsuarioPorUsername(String id) throws UsernameNotFoundException;
}
