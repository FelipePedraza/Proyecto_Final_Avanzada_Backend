package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.services.UsuarioDetallesServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioDetallesServicioImpl implements UsuarioDetallesServicio {

    private final UsuarioRepositorio usuarioRepositorio;

    @Override
    public UserDetails cargarUsuarioPorUsername(String id) throws UsernameNotFoundException {

        Usuario user = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRol().name()));

        return new org.springframework.security.core.userdetails.User(
                user.getId(),
                user.getContrasena(),
                authorities
        );
    }
}