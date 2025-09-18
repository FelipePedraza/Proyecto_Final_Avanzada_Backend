package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.usuario.*;
import co.edu.uniquindio.application.mappers.UsuarioMapper;
import co.edu.uniquindio.application.models.entitys.Usuario;
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
    public UsuarioDTO registro(CreacionAnfitrionDTO anfitrionDTO) throws Exception {

        if (usuarioRepositorio.findByEmail(anfitrionDTO.email()).isPresent()) {
            throw new Exception("El correo electrónico ya está en uso");
        }

        Usuario usuario = usuarioMapper.fromDTO(anfitrionDTO);
        usuario.setContrasena(passwordEncoder.encode(anfitrionDTO.contrasena()));

        Usuario saved = usuarioRepositorio.save(usuario);

        return usuarioMapper.toDTO(saved);
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