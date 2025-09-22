package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.usuario.*;
import co.edu.uniquindio.application.exceptions.NoResourceFoundException;
import co.edu.uniquindio.application.exceptions.ValueConflictException;
import co.edu.uniquindio.application.mappers.UsuarioMapper;
import co.edu.uniquindio.application.models.entitys.ContrasenaCodigoReinicio;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.models.enums.Estado;
import co.edu.uniquindio.application.repositories.ContrasenaCodigoReinicioRepositorio;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.services.UsuarioServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServicioImpl implements UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;
    private final ContrasenaCodigoReinicioRepositorio contrasenaCodigoReinicioRepositorio;


    @Override
    public void crear(CreacionUsuarioDTO usuarioDTO) throws Exception {

        if(existePorEmail(usuarioDTO.email())){
            throw new ValueConflictException("El email ya existe");
        }

        Usuario nuevoUsuario = usuarioMapper.toEntity(usuarioDTO);
        nuevoUsuario.setContrasena(passwordEncoder.encode(usuarioDTO.contrasena()));
        usuarioRepositorio.save(nuevoUsuario);
    }

    @Override
    public void editar(String id, EdicionUsuarioDTO usuarioDTO) throws Exception {
        Usuario usuario = obtenerUsuario(id);
        usuarioMapper.updateUsuarioFromDTO(usuarioDTO, usuario);
        usuarioRepositorio.save(usuario);
    }

    @Override
    public void eliminar(String id) throws Exception {
        Usuario usuario = obtenerUsuario(id);
        usuario.setEstado(Estado.ELIMINADO);
        usuarioRepositorio.save(usuario);

    }

    @Override
    public UsuarioDTO obtener(String id) throws Exception {
        Usuario usuario = obtenerUsuario(id);
        return usuarioMapper.toUserDTO(usuario);
    }

    @Override
    public void cambiarContrasena(CambioContrasenaDTO cambioContrasenaDTO) throws Exception {

        Usuario usuario = obtenerUsuario(cambioContrasenaDTO.id());

        if( passwordEncoder.matches(usuario.getContrasena(), cambioContrasenaDTO.contrasenaActual())){
            throw new ValueConflictException("La contraseña no coinciden con su contraseña actual");
        }

        if( passwordEncoder.matches(cambioContrasenaDTO.contrasenaNueva(), usuario.getContrasena())){
            throw new ValueConflictException("La contraseña no puede ser igual a la anterior");
        }

        usuario.setContrasena(passwordEncoder.encode(cambioContrasenaDTO.contrasenaNueva()));
        usuarioRepositorio.save(usuario);
    }

    @Override
    public void reiniciarContrasena(ReinicioContrasenaDTO reinicioContrasenaDTO) throws Exception {
        Optional<ContrasenaCodigoReinicio> contrasenaCodigoReinicio = contrasenaCodigoReinicioRepositorio.findByUsuario_Email(reinicioContrasenaDTO.email());

        if(contrasenaCodigoReinicio.isEmpty()){
            throw new NoResourceFoundException("El usuario no existe");
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

    @Override
    public void crearAnfitrion(CreacionAnfitrionDTO dto) throws Exception {

    }

    public boolean existePorEmail(String email){

        Optional<Usuario> optionalUsuario = usuarioRepositorio.findByEmail(email);

        return optionalUsuario.isPresent();
    }

    private Usuario obtenerUsuario(String id) throws Exception {
        Optional<Usuario> optionalUsuario =  usuarioRepositorio.findById(id);

        if(optionalUsuario.isEmpty()){
            throw new NoResourceFoundException("No se encontro el usuario con el id: " + id);
        }

        return optionalUsuario.get();
    }
}