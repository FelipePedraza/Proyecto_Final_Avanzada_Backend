package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.alojamiento.*;
import co.edu.uniquindio.application.dtos.usuario.EdicionUsuarioDTO;
import co.edu.uniquindio.application.dtos.usuario.UsuarioDTO;
import co.edu.uniquindio.application.models.entitys.Alojamiento;
import co.edu.uniquindio.application.repositories.AlojamientoRepositorio;
import co.edu.uniquindio.application.services.AlojamientoServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;
import co.edu.uniquindio.application.mappers.AlojamientoMapper;
import co.edu.uniquindio.application.mappers.UsuarioMapper;
import co.edu.uniquindio.application.models.entitys.Usuario;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlojamientoServicioImpl implements AlojamientoServicio {

    private final AlojamientoRepositorio alojamientoRepositorio;
    private final AlojamientoMapper alojamientoMapper;
    private final UsuarioServicioImpl usuarioServicio;
    private final UsuarioMapper usuarioMapper;


    @Override
    public void crear(CreacionAlojamientoDTO alojamientoDTO) throws Exception {

        //Se verifica que no se repita el titulo
        if(existePorTitulo(alojamientoDTO.titulo())){
            throw new Exception("El titulo ya existe");
        }

        //Se obtiene la informacion del usuario autenticado
        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();
        UsuarioDTO usuarioDTO = usuarioServicio.obtener(idUsuarioAutenticado);
        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);

        //Se comprueba que el usuario sea un anfitrion
        if(!usuario.getRol().name().equals("Anfitrion")){
            throw new AccessDeniedException("El usuario no es un anfitrion");
        }

        //Se crea el alojamiento
        Alojamiento nuevoAlojamiento = alojamientoMapper.toEntity(alojamientoDTO);
        nuevoAlojamiento.setAnfitrion(usuario);
        alojamientoRepositorio.save(nuevoAlojamiento);
    }

    @Override
    public void editar (Long id, EdicionUsuarioDTO edicionUsuarioDTO) throws Exception {

    }

    @Override
    public void eliminar(Long id) throws Exception {

    }

    @Override
    public Alojamiento obtenerAlojamientoId(Long id) throws Exception {
        return null;
    }

    @Override
    public MetricasDTO obtenerMetricas(Long id) throws Exception {
        return null;
    }

    @Override
    public List<ItemAlojamientoDTO> obtenerAlojamiento(AlojamientoFiltroDTO filtros) throws Exception {
        return null;
    }

    @Override
    public List<ItemAlojamientoDTO> obtenerAlojamientoUsuario(String id, int pagina) throws Exception {

        Pageable pageable = PageRequest.of(pagina, 5);
        Page<ItemAlojamientoDTO> alojamientos = alojamientoRepositorio.getAlojamientos(id, pageable);

        return alojamientos.toList();
    }

    public boolean existePorTitulo(String titulo){

        Optional<Alojamiento> optionalAlojamiento = alojamientoRepositorio.findByTitulo(titulo);

        return optionalAlojamiento.isPresent();
    }

}