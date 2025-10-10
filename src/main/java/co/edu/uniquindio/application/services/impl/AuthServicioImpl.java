package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.usuario.*;
import co.edu.uniquindio.application.exceptions.NoFoundException;
import co.edu.uniquindio.application.models.entitys.ContrasenaCodigoReinicio;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.repositories.ContrasenaCodigoReinicioRepositorio;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.security.JWTUtils;
import co.edu.uniquindio.application.services.AuthServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServicioImpl implements AuthServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final JWTUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final ContrasenaCodigoReinicioRepositorio contrasenaCodigoReinicioRepositorio;

    @Override
    public TokenDTO login(LoginDTO loginDTO) throws Exception {
        Optional<Usuario> optionalUsuario = usuarioRepositorio.findByEmail(loginDTO.email());

        if(optionalUsuario.isEmpty()){
            // Lanzar BadCredentialsException para que Spring Security lo traduzca a 401 cuando corresponda
            throw new BadCredentialsException("Credenciales inválidas");
        }

        Usuario usuario = optionalUsuario.get();

        // Verificar si la contraseña es correcta usando el PasswordEncoder
        if(!passwordEncoder.matches(loginDTO.contrasena(), usuario.getContrasena())){
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String token = jwtUtils.generarToken(usuario.getId(), crearReclamos(usuario));
        return new TokenDTO(token);
    }

    @Override
    public Boolean obtnerIdAutenticado(String idUsuario) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = user.getUsername();
        return idUsuarioAutenticado.equals(idUsuario);
    }

    @Override
    public void reiniciarContrasena(ReinicioContrasenaDTO reinicioContrasenaDTO) throws Exception {

        Optional<ContrasenaCodigoReinicio> contrasenaCodigoReinicio = contrasenaCodigoReinicioRepositorio.findByUsuario_Email(reinicioContrasenaDTO.email());

        if(contrasenaCodigoReinicio.isEmpty()){
            throw new NoFoundException("El usuario no existe");
        }

        ContrasenaCodigoReinicio contrasenaCodigoReinicioActualizado = contrasenaCodigoReinicio.get();

        if(!contrasenaCodigoReinicioActualizado.getCodigo().equals(reinicioContrasenaDTO.codigoVerificacion())){
            throw new Exception("El codigo no es válido");
        }

        if ( contrasenaCodigoReinicioActualizado.getCreadoEn().plusMinutes(15).isBefore(LocalDateTime.now())){
            throw new Exception("El codigo ya vencio, solicite otro");
        }

        Usuario usuario = contrasenaCodigoReinicioActualizado.getUsuario();
        usuario.setContrasena(passwordEncoder.encode(reinicioContrasenaDTO.nuevaContrasena()));
        usuarioRepositorio.save(usuario);

    }

    private Map<String, String> crearReclamos(Usuario usuario){
        return Map.of(
                "email", usuario.getEmail(),
                "name", usuario.getNombre(),
                "role", "ROL_"+usuario.getRol().name()
        );
    }

}
