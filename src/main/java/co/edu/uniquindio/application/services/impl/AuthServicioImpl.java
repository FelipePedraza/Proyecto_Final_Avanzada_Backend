package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.EmailDTO;
import co.edu.uniquindio.application.dtos.usuario.*;
import co.edu.uniquindio.application.exceptions.NoFoundException;
import co.edu.uniquindio.application.exceptions.ValidationException;
import co.edu.uniquindio.application.models.entitys.ContrasenaCodigoReinicio;
import co.edu.uniquindio.application.models.entitys.Resena;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.models.enums.Estado;
import co.edu.uniquindio.application.repositories.ContrasenaCodigoReinicioRepositorio;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.security.JWTUtils;
import co.edu.uniquindio.application.services.AuthServicio;
import co.edu.uniquindio.application.services.EmailServicio;
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
    private final EmailServicio emailServicio;

    @Override
    public TokenDTO login(LoginDTO loginDTO) throws Exception {
        Optional<Usuario> optionalUsuario = usuarioRepositorio.findByEmail(loginDTO.email());

        if(optionalUsuario.isEmpty()){
            // Lanzar BadCredentialsException para que Spring Security lo traduzca a 401 cuando corresponda
            throw new BadCredentialsException("Credenciales inválidas");
        }

        Usuario usuario = optionalUsuario.get();

        if(usuario.getEstado().equals(Estado.ELIMINADO)){
            throw new NoFoundException("Usuario no encontrado");
        }

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
    public void solicitarRecuperacion(OlvidoContrasenaDTO olvidoContrasenaDTO) throws Exception {

        // Buscar usuario por email
        Optional<Usuario> optionalUsuario = usuarioRepositorio.findByEmail(olvidoContrasenaDTO.email());

        if (optionalUsuario.isEmpty()) {
            throw new NoFoundException("No existe un usuario con ese correo electrónico");
        }

        Usuario usuario = optionalUsuario.get();

        // Validar que el usuario esté activo
        if (usuario.getEstado() == Estado.ELIMINADO) {
            throw new ValidationException("El usuario no está activo");
        }

        // Generar código de 6 dígitos aleatorio
        String codigo = generarCodigoRecuperacion();

        // Buscar si ya existe un código para este usuario
        Optional<ContrasenaCodigoReinicio> codigoExistente =
                contrasenaCodigoReinicioRepositorio.findByUsuario_Email(olvidoContrasenaDTO.email());

        ContrasenaCodigoReinicio contrasenaCodigoReinicio;

        if (codigoExistente.isPresent()) {
            // Actualizar código existente
            contrasenaCodigoReinicio = codigoExistente.get();
            contrasenaCodigoReinicio.setCodigo(codigo);
            contrasenaCodigoReinicio.setCreadoEn(LocalDateTime.now());
        } else {
            // Crear nuevo registro
            contrasenaCodigoReinicio = ContrasenaCodigoReinicio.builder()
                    .codigo(codigo)
                    .creadoEn(LocalDateTime.now())
                    .usuario(usuario)
                    .build();
        }

        // Guardar código en la base de datos
        contrasenaCodigoReinicioRepositorio.save(contrasenaCodigoReinicio);

        // Enviar codigo al correo
        enviarEmailCodigo(codigo, usuario);


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
                "rol", "ROL_"+usuario.getRol().name()
        );
    }

    /**
     * Genera un código aleatorio de 6 dígitos para recuperación de contraseña
     */
    private String generarCodigoRecuperacion() {
        int codigo = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(codigo);
    }

    /**
     * Enviar correo con el codigo
     */
    private void enviarEmailCodigo(String codigo, Usuario usuario) {

        String asunto = "Código de recuperación de contraseña - ViviGo";
        String cuerpo = String.format(
                "Hola %s,\n\n" +
                        "Has solicitado recuperar tu contraseña.\n\n" +
                        "Tu código de verificación es: %s\n\n" +
                        "Este código expirará en 15 minutos.\n\n" +
                        "Si no solicitaste este cambio, por favor ignora este correo.\n\n" +
                        "Saludos,\n" +
                        "Equipo ViviGo",
                usuario.getNombre(),
                codigo
        );

        try {
            emailServicio.enviarEmail(new EmailDTO(
                    asunto,
                    cuerpo,
                    usuario.getEmail()
            ));
        } catch (Exception e) {
            System.err.println("Error enviando email de respuesta a reseña: " + e.getMessage());
        }
    }

}
