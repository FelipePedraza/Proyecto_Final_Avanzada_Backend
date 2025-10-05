package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.EmailDTO;
import co.edu.uniquindio.application.dtos.usuario.*;
import co.edu.uniquindio.application.exceptions.NoFoundException;
import co.edu.uniquindio.application.exceptions.ValidationException;
import co.edu.uniquindio.application.exceptions.ValueConflictException;
import co.edu.uniquindio.application.mappers.UsuarioMapper;
import co.edu.uniquindio.application.mappers.PerfilAnfitrionMapper;
import co.edu.uniquindio.application.models.entitys.ContrasenaCodigoReinicio;
import co.edu.uniquindio.application.models.entitys.PerfilAnfitrion;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.models.enums.Estado;
import co.edu.uniquindio.application.models.enums.Rol;
import co.edu.uniquindio.application.repositories.ContrasenaCodigoReinicioRepositorio;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.services.AuthServicio;
import co.edu.uniquindio.application.services.EmailServicio;
import co.edu.uniquindio.application.services.ImagenServicio;
import co.edu.uniquindio.application.services.UsuarioServicio;
import org.springframework.security.access.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import co.edu.uniquindio.application.repositories.PerfilAnfitrionRepositorio;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServicioImpl implements UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final UsuarioMapper usuarioMapper;
    private final ContrasenaCodigoReinicioRepositorio contrasenaCodigoReinicioRepositorio;
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
    public void editar(String id, EdicionUsuarioDTO usuarioDTO, MultipartFile file) throws Exception {

        if(!authServicio.obtnerIdAutenticado(id)){
            throw new AccessDeniedException("No tiene permisos para editar de este usuario.");
        }

        String actualizadaPublicId = null;
        String viejaPublicId = null; // Para almacenar el public_id anterior

        try {
            // Obtener el usuario actual
            Usuario usuario = obtenerUsuarioId(id);

            // Guardar el public_id anterior si existe
            if (usuario.getFoto() != null) {
                // Extraer el public_id de la URL de Cloudinary
                // Asumiendo que la URL es algo como: https://res.cloudinary.com/.../Vivi_Go/Perfiles/abc123.jpg
                // Necesitamos extraer "Vivi_Go/Perfiles/abc123"
                String fotoUrl = usuario.getFoto();
                viejaPublicId = imagenServicio.extraerPublicIdDelUrl(fotoUrl);
            }

            // Subir imagen si fue proporcionada
            if (file != null && !file.isEmpty()) {
                Map uploadResp = imagenServicio.actualizar(file, "Vivi_Go/Perfiles");
                actualizadaPublicId = (String) uploadResp.get("public_id");
                String secureUrl = (String) uploadResp.get("secure_url");

                // Clonar el DTO con la nueva URL de imagen
                usuarioDTO = new EdicionUsuarioDTO(
                        usuarioDTO.nombre(),
                        usuarioDTO.telefono(),
                        secureUrl,
                        usuarioDTO.fechaNacimiento()
                );

                // Eliminar la foto anterior SI existe y SI se subió una nueva exitosamente
                if (viejaPublicId != null) {
                    try {
                        imagenServicio.eliminar(viejaPublicId);
                    } catch (Exception e) {
                        System.out.println("Advertencia: No se pudo eliminar la foto anterior: " + viejaPublicId);
                        // logear este error
                    }
                }
            }

            usuarioMapper.updateUsuarioFromDTO(usuarioDTO, usuario);
            usuarioRepositorio.save(usuario);

        } catch (Exception ex) {
            // Si ocurre un error, eliminar la imagen recién subida (si se subió)
            if (actualizadaPublicId != null) {
                try {
                    imagenServicio.eliminar(actualizadaPublicId);
                } catch (Exception ignored) {
                    // loggear este fallo
                }
            }
            throw ex;
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

    @Override
    public void crearAnfitrion(CreacionAnfitrionDTO dto) throws Exception {

        // Verificar que el usuario autenticado coincide con el id del DTO (permiso)
        if (!authServicio.obtnerIdAutenticado(dto.usuarioId())) {
            throw new AccessDeniedException("No tiene permisos para crear perfil de anfitrión para este usuario.");
        }

        Usuario usuario = obtenerUsuarioId(dto.usuarioId());
        
        if (usuario.getRol() == Rol.Anfitrion) {
            throw new ValueConflictException("El usuario ya es un anfitrion");
        }

        // Crear perfil de anfitrión
        PerfilAnfitrion perfil = perfilAnfitrionMapper.toEntity(dto);
        perfil.setUsuario(usuario);
        perfilAnfitrionRepositorio.save(perfil);

        // Actualizar la relación bidireccional
        usuario.setRol(Rol.Anfitrion);
        usuario.setPerfilAnfitrion(perfil);
        usuarioRepositorio.save(usuario);

        // Enviar email de confirmación
        emailServicio.enviarEmail(new EmailDTO(
                "¡Felicidades! Ahora eres Anfitrión en ViviGo",
                "Tu perfil de anfitrión ha sido creado exitosamente. Ahora puedes publicar tus alojamientos y comenzar a recibir reservas.",
                usuario.getEmail()
        ));
    }

    public boolean existePorEmail(String email){

        Optional<Usuario> optionalUsuario = usuarioRepositorio.findByEmail(email);

        return optionalUsuario.isPresent();
    }

    private Usuario obtenerUsuarioId(String id) throws Exception {
        Optional<Usuario> optionalUsuario =  usuarioRepositorio.findById(id);

        if(optionalUsuario.isEmpty()){
            throw new NoFoundException("No se encontro el usuario con el id: " + id);
        }

        return optionalUsuario.get();
    }
}