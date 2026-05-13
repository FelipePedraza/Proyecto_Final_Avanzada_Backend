package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.EmailDTO;
import co.edu.uniquindio.application.dtos.usuario.*;
import co.edu.uniquindio.application.exceptions.NoFoundException;
import co.edu.uniquindio.application.exceptions.ValidationException;
import co.edu.uniquindio.application.models.entitys.ContrasenaCodigoReinicio;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.models.enums.Estado;
import co.edu.uniquindio.application.repositories.ContrasenaCodigoReinicioRepositorio;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.security.JWTUtils;
import co.edu.uniquindio.application.services.AuthServicio;
import co.edu.uniquindio.application.services.EmailServicio;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.micrometer.core.instrument.MeterRegistry;
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
    private final MeterRegistry meterRegistry;

    @Override
    public TokenDTO login(LoginDTO loginDTO) throws Exception {
        Optional<Usuario> optionalUsuario = usuarioRepositorio.findByEmail(loginDTO.email());

        if(optionalUsuario.isEmpty()){
            meterRegistry.counter("auth.login.fallido").increment();
            throw new BadCredentialsException("Credenciales inválidas");
        }

        Usuario usuario = optionalUsuario.get();

        if(usuario.getEstado().equals(Estado.ELIMINADO)){
            meterRegistry.counter("auth.login.fallido").increment();
            throw new NoFoundException("Usuario no encontrado");
        }

        if(!passwordEncoder.matches(loginDTO.contrasena(), usuario.getContrasena())){
            meterRegistry.counter("auth.login.fallido").increment();
            throw new BadCredentialsException("Credenciales inválidas");
        }

        Map<String, String> claims = crearReclamos(usuario);
        String token = jwtUtils.generarToken(usuario.getId(), claims);
        String refreshToken = jwtUtils.generarRefreshToken(usuario.getId(), claims);
        meterRegistry.counter("auth.login.exitoso").increment();
        return new TokenDTO(token, refreshToken);
    }

    @Override
    public TokenDTO refrescarToken(RefreshTokenDTO refreshTokenDTO) throws Exception {
        String refreshToken = refreshTokenDTO.refreshToken();

        try {
            Jws<Claims> jws = jwtUtils.decodificarJwt(refreshToken);
            String idUsuario = jws.getPayload().getSubject();

            Usuario usuario = usuarioRepositorio.findById(idUsuario)
                    .orElseThrow(() -> new NoFoundException("Usuario no encontrado"));

            if (usuario.getEstado() == Estado.ELIMINADO) {
                throw new NoFoundException("Usuario no encontrado");
            }

            Map<String, String> claims = crearReclamos(usuario);

            String nuevoToken = jwtUtils.generarToken(usuario.getId(), claims);
            String nuevoRefreshToken = jwtUtils.generarRefreshToken(usuario.getId(), claims);

            return new TokenDTO(nuevoToken, nuevoRefreshToken);

        } catch (Exception e) {
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }
    }
    @Override
    public Boolean obtnerIdAutenticado(String idUsuario) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = user.getUsername();
        return idUsuarioAutenticado.equals(idUsuario);
    }

    @Override
    public void solicitarRecuperacion(OlvidoContrasenaDTO olvidoContrasenaDTO) throws Exception {

        Optional<Usuario> optionalUsuario = usuarioRepositorio.findByEmail(olvidoContrasenaDTO.email());

        if (optionalUsuario.isEmpty()) {
            throw new NoFoundException("No existe un usuario con ese correo electrónico");
        }

        Usuario usuario = optionalUsuario.get();

        if (usuario.getEstado() == Estado.ELIMINADO) {
            throw new ValidationException("El usuario no está activo");
        }

        String codigo = generarCodigoRecuperacion();

        Optional<ContrasenaCodigoReinicio> codigoExistente =
                contrasenaCodigoReinicioRepositorio.findByUsuario_Email(olvidoContrasenaDTO.email());

        ContrasenaCodigoReinicio contrasenaCodigoReinicio;

        if (codigoExistente.isPresent()) {
            contrasenaCodigoReinicio = codigoExistente.get();
            contrasenaCodigoReinicio.setCodigo(codigo);
            contrasenaCodigoReinicio.setCreadoEn(LocalDateTime.now());
        } else {
            contrasenaCodigoReinicio = ContrasenaCodigoReinicio.builder()
                    .codigo(codigo)
                    .creadoEn(LocalDateTime.now())
                    .usuario(usuario)
                    .build();
        }

        contrasenaCodigoReinicioRepositorio.save(contrasenaCodigoReinicio);
        enviarEmailCodigo(codigo, usuario);
        meterRegistry.counter("auth.recuperacion.solicitada").increment();
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

    private String generarCodigoRecuperacion() {
        int codigo = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(codigo);
    }

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
