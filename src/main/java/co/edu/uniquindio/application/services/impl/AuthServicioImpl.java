package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.usuario.*;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.security.JWTUtils;
import co.edu.uniquindio.application.services.AuthServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServicioImpl implements AuthServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final JWTUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public TokenDTO login(LoginDTO loginDTO) throws Exception {

        Optional<Usuario> optionalUsuario = usuarioRepositorio.findByEmail(loginDTO.email());

        if(optionalUsuario.isEmpty()){
            throw new Exception("El usuario no existe");
        }

        Usuario usuario = optionalUsuario.get();

        // Verificar si la contraseña es correcta usando el PasswordEncoder
        if(!passwordEncoder.matches(loginDTO.contrasena(), usuario.getContrasena())){
            throw new Exception("El usuario no existe");
        }

        String token = jwtUtils.generarToken(usuario.getId(), crearReclamos(usuario));
        return new TokenDTO(token);
    }

    private Map<String, String> crearReclamos(Usuario usuario){
        return Map.of(
                "email", usuario.getEmail(),
                "name", usuario.getNombre(),
                "role", "ROL_"+usuario.getRol().name()
        );
    }

}
