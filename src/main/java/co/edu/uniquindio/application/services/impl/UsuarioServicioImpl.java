package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.Usuarios.CreacionUsuarioDTO;
import co.edu.uniquindio.application.dtos.Usuarios.EdicionUsuarioDTO;
import co.edu.uniquindio.application.dtos.Usuarios.CambioContrasenaDTO;
import co.edu.uniquindio.application.dtos.Usuarios.UsuarioDTO;
import co.edu.uniquindio.application.exceptions.ResourceNotFoundException;
import co.edu.uniquindio.application.mappers.UsuarioMapper;
import co.edu.uniquindio.application.models.entitys.Usuario;
import co.edu.uniquindio.application.repositories.UsuarioRepositorio;
import co.edu.uniquindio.application.services.UsuarioServicio;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioServicioImpl implements UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServicioImpl(UsuarioRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioDTO crear(CreacionUsuarioDTO dto) throws Exception {
        if (usuarioRepositorio.findByEmail(dto.email()).isPresent()) {
            throw new Exception("Ya existe un usuario con ese correo");
        }
        Usuario u = UsuarioMapper.toEntity(dto);
        Usuario saved = usuarioRepositorio.save(u);
        return UsuarioMapper.toDTO(saved);
    }

    @Override
    public UsuarioDTO editar(EdicionUsuarioDTO dto) throws Exception {
        Usuario existing = usuarioRepositorio.findById(dto.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        existing.setNombre(dto.nombre());
        existing.setTelefono(dto.telefono());
        existing.setFoto(dto.foto());
        existing.setRol(dto.rol());
        Usuario saved = usuarioRepositorio.save(existing);
        return UsuarioMapper.toDTO(saved);
    }

    @Override
    public void eliminar(Long id) throws Exception {
        Usuario existing = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        usuarioRepositorio.delete(existing);
    }

    @Override
    public UsuarioDTO obtener(Long id) throws Exception {
        Usuario u = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return UsuarioMapper.toDTO(u);
    }

    @Override
    public List<UsuarioDTO> listAll() {
        return usuarioRepositorio.findAll().stream().map(UsuarioMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public void cambiarContrasena(Long id, CambioContrasenaDTO dto) throws Exception {
        Usuario u = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        // Aquí podrías verificar la contraseña anterior si la incluyes en el DTO.
        u.setContrasena(passwordEncoder.encode(dto.newPassword()));
        usuarioRepositorio.save(u);
    }
}