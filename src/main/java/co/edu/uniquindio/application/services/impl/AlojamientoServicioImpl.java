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
import org.springframework.web.multipart.MultipartFile;
import co.edu.uniquindio.application.services.UsuarioServicio;
import co.edu.uniquindio.application.services.ImagenServicio;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlojamientoServicioImpl implements AlojamientoServicio {

    private final AlojamientoRepositorio alojamientoRepositorio;
    private final AlojamientoMapper alojamientoMapper;
    private final UsuarioServicio usuarioServicio;
    private final UsuarioMapper usuarioMapper;
    private final ImagenServicio imagenServicio;

    @Override
    public void crear(CreacionAlojamientoDTO alojamientoDTO, MultipartFile[] archivos) throws Exception {

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

        // Lista para almacenar public_ids subidas (para limpiar en caso de fallo)
        List<String> publicIdsSubidos = new ArrayList<>();
        List<String> urlsSeguras = new ArrayList<>();

        try {
            // Si vienen archivos, subirlos
            if (archivos != null) {
                for (MultipartFile archivo : archivos) {
                    if (archivo != null && !archivo.isEmpty()) {
                        Map uploadResp = imagenServicio.actualizar(archivo, "Vivi_Go/Alojamientos");
                        String publicId = (String) uploadResp.get("public_id");
                        String secureUrl = (String) uploadResp.get("secure_url");
                        if (publicId != null) publicIdsSubidos.add(publicId);
                        if (secureUrl != null) urlsSeguras.add(secureUrl);
                    }
                }
            }

            // Construir y guardar alojamiento con las URLs
            Alojamiento nuevoAlojamiento = alojamientoMapper.toEntity(alojamientoDTO);
            nuevoAlojamiento.setImagenes(urlsSeguras);
            nuevoAlojamiento.setAnfitrion(usuario);
            alojamientoRepositorio.save(nuevoAlojamiento);

        } catch (Exception ex) {
            // Si hay un error, eliminar las imágenes que se subieron
            for (String pid : publicIdsSubidos) {
                try {
                    imagenServicio.eliminar(pid);
                } catch (Exception ignored) {
                    // loggear
                }
            }
            throw ex;
        }
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