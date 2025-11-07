package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.EmailDTO;
import co.edu.uniquindio.application.dtos.usuario.*;
import co.edu.uniquindio.application.exceptions.NoFoundException;
import co.edu.uniquindio.application.exceptions.ValidationException;
import co.edu.uniquindio.application.exceptions.ValueConflictException;
import co.edu.uniquindio.application.mappers.UsuarioMapper;
import co.edu.uniquindio.application.mappers.PerfilAnfitrionMapper;
import co.edu.uniquindio.application.models.entitys.PerfilAnfitrion;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.models.enums.Estado;
import co.edu.uniquindio.application.models.enums.Rol;
import co.edu.uniquindio.application.repositories.*;
import co.edu.uniquindio.application.services.*;
import org.springframework.security.access.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServicioImpl implements UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthServicio authServicio;
    private final EmailServicio emailServicio;
    private final ImagenServicio imagenServicio;
    private final PerfilAnfitrionRepositorio perfilAnfitrionRepositorio;
    private final PerfilAnfitrionMapper perfilAnfitrionMapper;




    @Override
    public void crear(CreacionUsuarioDTO usuarioDTO) throws Exception {

        if(existePorEmail(usuarioDTO.email())){
            throw new ValueConflictException("El email ya existe");
        }

        Usuario nuevoUsuario = usuarioMapper.toEntity(usuarioDTO);
        nuevoUsuario.setContrasena(passwordEncoder.encode(usuarioDTO.contrasena()));
        usuarioRepositorio.save(nuevoUsuario);

        emailServicio.enviarEmail(new EmailDTO("Registro Exitoso", "El usuario se ha registrado correctamente", nuevoUsuario.getEmail()));
    }

    @Override
    public void editar(String id, EdicionUsuarioDTO usuarioDTO) {

        if(!authServicio.obtnerIdAutenticado(id)){
            throw new AccessDeniedException("No tiene permisos para editar de este usuario.");
        }

        // Obtener el usuario actual
        Usuario usuario = obtenerUsuarioId(id);

        // Guardar la URL de la foto *antigua* ANTES de mapear
        String urlFotoActual = usuario.getFoto();
        String urlFotoNueva = usuarioDTO.foto(); // URL que envía el front

        // Aplicar los cambios del DTO al 'usuario'
        // Esto incluye la nueva URL de la foto
        usuarioMapper.updateUsuarioFromDTO(usuarioDTO, usuario);

        // Guardar el usuario en la BD
        usuarioRepositorio.save(usuario);

        // Lógica de limpieza (POST-guardado)
        // Si la foto cambió Y había una foto antigua, eliminar la antigua de Cloudinary
        boolean fotoCambio = (urlFotoActual != null && !urlFotoActual.equals(urlFotoNueva)) ||
                (urlFotoActual == null && urlFotoNueva != null); // Caso: no tenía foto y ahora sí

        if (fotoCambio && urlFotoActual != null) {
            try {
                String viejaPublicId = imagenServicio.extraerPublicIdDelUrl(urlFotoActual);
                if (viejaPublicId != null && !viejaPublicId.isBlank()) {
                    imagenServicio.eliminar(viejaPublicId);
                }
            } catch (Exception e) {
                // Loggear este error, pero no fallar la transacción, ya que el usuario SÍ se actualizó
                System.err.println("Advertencia: No se pudo eliminar la foto anterior: " + urlFotoActual + ". Error: " + e.getMessage());
            }
        }

    }

    @Override
    public void eliminar(String id) throws Exception {
        Usuario usuario = obtenerUsuarioId(id);
        usuario.setEstado(Estado.ELIMINADO);
        usuarioRepositorio.save(usuario);
    }

    @Override
    public UsuarioDTO obtener(String id) throws Exception {
        Usuario usuario = obtenerUsuarioId(id);
        return usuarioMapper.toUserDTO(usuario);
    }

    @Override
    public void cambiarContrasena(String id, CambioContrasenaDTO cambioContrasenaDTO) throws Exception {

        Usuario usuario = obtenerUsuarioId(id);

        if(!authServicio.obtnerIdAutenticado(id)){
            // Si el usuario no está autorizado a cambiar la contraseña de otro usuario,
            // lanzamos AccessDeniedException para que se traduzca a 403 Forbidden.
            throw new AccessDeniedException("No tiene permisos para cambiar la contraseña de este usuario.");
        }

        // Verificar que la contraseña actual coincida
        if(!passwordEncoder.matches(cambioContrasenaDTO.contrasenaActual(), usuario.getContrasena())){
            throw new ValidationException("La contraseña actual es incorrecta.");
        }

        // Verificar que la nueva contraseña sea diferente a la actual
        if(cambioContrasenaDTO.contrasenaActual().equals(cambioContrasenaDTO.contrasenaNueva())){
            throw new ValueConflictException("La nueva contraseña no puede ser igual a la actual.");
        }

        usuario.setContrasena(passwordEncoder.encode(cambioContrasenaDTO.contrasenaNueva()));
        usuarioRepositorio.save(usuario);
    }

    @Override
    public void crearAnfitrion(CreacionAnfitrionDTO dto) throws Exception {

        // Verificar que el usuario autenticado coincide con el id del DTO (permiso)
        if (!authServicio.obtnerIdAutenticado(dto.usuarioId())) {
            throw new AccessDeniedException("No tiene permisos para crear perfil de anfitrión para este usuario.");
        }

        Usuario usuario = obtenerUsuarioId(dto.usuarioId());

        boolean esAnfitrion = usuario.getEsAnfitrion() != null && usuario.getEsAnfitrion();

        if (esAnfitrion) {
            throw new ValueConflictException("El usuario ya es un anfitrion");
        }

        // Crear perfil de anfitrión
        PerfilAnfitrion perfil = perfilAnfitrionMapper.toEntity(dto);
        perfil.setUsuario(usuario);
        perfilAnfitrionRepositorio.save(perfil);

        // Actualizar la relación bidireccional
        usuario.setRol(Rol.Anfitrion);
        usuario.setEsAnfitrion(true);
        usuario.setPerfilAnfitrion(perfil);
        usuarioRepositorio.save(usuario);

        // Enviar email de confirmación
        emailServicio.enviarEmail(new EmailDTO(
                "¡Felicidades! Ahora eres Anfitrión en ViviGo",
                "Tu perfil de anfitrión ha sido creado exitosamente. Ahora puedes publicar tus alojamientos y comenzar a recibir reservas.",
                usuario.getEmail()
        ));
    }

    @Override
    public AnfitrionPerfilDTO obtenerAnfitrion(String id) throws Exception {
        Usuario usuario = obtenerUsuarioId(id);
        return perfilAnfitrionMapper.toAnfitrionPerfilDTO(usuario.getPerfilAnfitrion());
    }

    public boolean existePorEmail(String email){

        Optional<Usuario> optionalUsuario = usuarioRepositorio.findByEmail(email);

        return optionalUsuario.isPresent();
    }

    private Usuario obtenerUsuarioId(String id) {
        Optional<Usuario> optionalUsuario =  usuarioRepositorio.findById(id);

        if(optionalUsuario.isEmpty()){
            throw new NoFoundException("No se encontro el usuario con el id: " + id);
        }

        return optionalUsuario.get();
    }

}