package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.usuario.*;
import co.edu.uniquindio.application.mappers.UsuarioMapper;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.services.AuthServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServicioImpl implements AuthServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    public TokenDTO login(LoginDTO loginDTO) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

}
