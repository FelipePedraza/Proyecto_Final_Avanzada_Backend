package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.alojamiento.*;
import co.edu.uniquindio.application.dtos.usuario.UsuarioDTO;
import co.edu.uniquindio.application.exceptions.NoFoundException;
import co.edu.uniquindio.application.exceptions.ValidationException;
import co.edu.uniquindio.application.models.entitys.Alojamiento;
import co.edu.uniquindio.application.models.enums.ReservaEstado;
import co.edu.uniquindio.application.repositories.AlojamientoRepositorio;
import co.edu.uniquindio.application.repositories.ReservaRepositorio;
import co.edu.uniquindio.application.services.AlojamientoServicio;
import co.edu.uniquindio.application.services.AuthServicio;
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
import co.edu.uniquindio.application.models.enums.Estado;

import java.time.LocalDate;
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
    private final AuthServicio authServicio;
    private final ReservaRepositorio reservaRepositorio;

    @Override
    public void crear(CreacionAlojamientoDTO alojamientoDTO, MultipartFile[] imagenes) throws Exception {

        //Se verifica que no se repita el titulo
        if(existePorTitulo(alojamientoDTO.titulo())){
            throw new Exception("El titulo ya existe");
        }
        
        //Se obtiene la informacion del usuario autenticado
        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();
        UsuarioDTO usuarioDTO = usuarioServicio.obtener(idUsuarioAutenticado);
        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);

        boolean esAnfitrion = usuario.getEsAnfitrion() != null && usuario.getEsAnfitrion();

        if (!esAnfitrion) {
            throw new AccessDeniedException("El usuario no es un anfitrion");
        }

        // Lista para almacenar public_ids subidas (para limpiar en caso de fallo)
        List<String> publicIdsSubidos = new ArrayList<>();
        List<String> urlsSeguras = new ArrayList<>();

        try {
            // Si vienen imagenes, subirlos
            if (imagenes != null) {
                for (MultipartFile archivo : imagenes) {
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
    public void editar (Long id, EdicionAlojamientoDTO edicionAlojamientoDTO, MultipartFile[] imagenes) throws Exception {

        // Obtener alojamiento existente
        Alojamiento alojamiento = obtenerAlojamientoId(id);

        // Permisos: solo anfitrión propietario puede editar
        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();
        UsuarioDTO usuarioDTO = usuarioServicio.obtener(idUsuarioAutenticado);
        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);

        boolean esAnfitrion = usuario.getEsAnfitrion() != null && usuario.getEsAnfitrion();

        if (!esAnfitrion) {
            throw new AccessDeniedException("El usuario no es un anfitrion");
        }

        if (alojamiento.getAnfitrion() == null || !alojamiento.getAnfitrion().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("No tiene permiso para editar este alojamiento");
        }

        // Copia de imágenes actuales
        List<String> actuales = alojamiento.getImagenes() != null ? new ArrayList<>(alojamiento.getImagenes()) : new ArrayList<>();

        // Validar cambio de título (si viene)
        if (edicionAlojamientoDTO.titulo() != null && !edicionAlojamientoDTO.titulo().equalsIgnoreCase(alojamiento.getTitulo())) {
            if (existePorTitulo(edicionAlojamientoDTO.titulo())) {
                throw new Exception("El título ya existe");
            }
        }

        // Aplicar cambios parciales con el mapper
        alojamientoMapper.updateAlojamientoFromDto(edicionAlojamientoDTO, alojamiento);

        // Según tu regla: edicionAlojamientoDTO.imagenes() == null => el cliente eliminó todas las imágenes.
        List<String> deseadas = edicionAlojamientoDTO.imagenes() != null ? new ArrayList<>(edicionAlojamientoDTO.imagenes()) : null;

        // Detectar si vienen archivos en multipart
        boolean hayArchivos = false;
        if (imagenes != null) {
            for (MultipartFile f : imagenes) {
                if (f != null && !f.isEmpty()) { 
                    hayArchivos = true; 
                    break; 
                }
            }
        }

        // Si cliente eliminó todas las imágenes y no envía nuevas, y además no había actuales -> inválido
        if (deseadas == null && !hayArchivos && (actuales == null || actuales.isEmpty())) {
            throw new Exception("El alojamiento debe tener al menos una imagen");
        }

        // Auxiliares para rollback y tracking
        List<String> publicIdsSubidos = new ArrayList<>();
        List<String> urlsNuevas = new ArrayList<>();

        // 1) Subir nuevas imágenes (si vienen). Si falla alguna subida, borrar las ya subidas y lanzar excepción.
        if (hayArchivos) {
            for (MultipartFile archivo : imagenes) {
                if (archivo != null && !archivo.isEmpty()) {
                    Map<String, Object> uploadResp;
                    try {
                        uploadResp = imagenServicio.actualizar(archivo, "Vivi_Go/Alojamientos");
                    } catch (Exception ex) {
                        // rollback de subidas parciales
                        for (String pid : publicIdsSubidos) {
                            try { 
                                imagenServicio.eliminar(pid); 
                            } catch (Exception ignored) {
                                // loggear
                            }
                        }
                        throw new Exception("Error subiendo imágenes", ex);
                    }
                    String publicId = (String) uploadResp.get("public_id");
                    String secureUrl = (String) uploadResp.get("secure_url");
                    if (publicId != null) publicIdsSubidos.add(publicId);
                    if (secureUrl != null) urlsNuevas.add(secureUrl);
                }
            }
        }

        // 2) Construir lista final de imágenes:
        List<String> finalImgs = new ArrayList<>();
        if (deseadas != null) {
            finalImgs.addAll(deseadas);
        }
        for (String u : urlsNuevas) if (!finalImgs.contains(u)) finalImgs.add(u);

        // 3) Validar al menos una imagen
        if (finalImgs.isEmpty()) {
            // rollback de nuevas subidas
            for (String pid : publicIdsSubidos) {
                try { imagenServicio.eliminar(pid); } catch (Exception ignored) {}
            }
            throw new Exception("El alojamiento debe tener al menos una imagen");
        }

        // 4) Guardar en BD la lista final (si falla, revertir nuevas subidas)
        alojamiento.setImagenes(finalImgs);
        try {
            alojamientoRepositorio.save(alojamiento);
        } catch (Exception bdEx) {
            for (String pid : publicIdsSubidos) {
                try { 
                    imagenServicio.eliminar(pid); 
                } catch (Exception ignored) {
                    // loggear
                }
            }
            throw new Exception("Error guardando alojamiento (BD). Se revirtieron nuevas subidas.", bdEx);
        }

        // 5) Calcular imágenes a eliminar: actuales - deseadas (si deseadas != null), si deseadas == null => eliminar todas las actuales
        List<String> aEliminar = new ArrayList<>();
        if (deseadas != null) {
            for (String url : new ArrayList<>(actuales)) {
                if (!deseadas.contains(url)) aEliminar.add(url);
            }
        } else {
            aEliminar.addAll(actuales);
        }

        // 6) Eliminar antiguas en Cloudinary
        for (String url : aEliminar) {
            try {
                String publicId = imagenServicio.extraerPublicIdDelUrl(url);
                if (publicId != null && !publicId.isBlank()) {
                    imagenServicio.eliminar(publicId);
                } else {
                    imagenServicio.eliminar(url);
                }
            } catch (Exception ignored) {
                // loggear
            }
        }
    }

    @Override
    public void eliminar(Long id) throws Exception {
        // Obtener alojamiento existente
        Alojamiento alojamiento = obtenerAlojamientoId(id);

        // Permisos: solo anfitrión propietario puede eliminar
        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();
        UsuarioDTO usuarioDTO = usuarioServicio.obtener(idUsuarioAutenticado);
        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);

        boolean esAnfitrion = usuario.getEsAnfitrion() != null && usuario.getEsAnfitrion();

        if (!esAnfitrion) {
            throw new AccessDeniedException("El usuario no es un anfitrion");
        }

        if (alojamiento.getAnfitrion() == null || !alojamiento.getAnfitrion().getId().equals(usuario.getId())) {
            throw new AccessDeniedException("No tiene permiso para eliminar este alojamiento");
        }
        // La eliminación es lógica
        alojamiento.setEstado(Estado.ELIMINADO);
        alojamientoRepositorio.save(alojamiento);
    }

    @Override
    public AlojamientoDTO obtenerPorId(Long id) throws Exception {
        Alojamiento alojamiento = obtenerAlojamientoId(id);
        return alojamientoMapper.toDTO(alojamiento);
    }

    @Override
    public MetricasDTO obtenerMetricas(Long id) throws Exception {

        // Obtener usuario autenticado
        User usuarioAutenticado = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String idUsuarioAutenticado = usuarioAutenticado.getUsername();

        // Obtener alojamiento
        Alojamiento alojamiento = obtenerAlojamientoId(id);

        // Verificar que el usuario autenticado sea el anfitrión del alojamiento
        if (!alojamiento.getAnfitrion().getId().equals(idUsuarioAutenticado)) {
            throw new AccessDeniedException("No tienes permiso para ver las métricas de este alojamiento");
        }

        // Obtener métricas de reseñas
        Integer totalResenas = alojamiento.getNumeroCalificaciones() != null ?
                alojamiento.getNumeroCalificaciones() : 0;

        Double promedioCalificaciones = alojamiento.getPromedioCalificaciones() != null ?
                alojamiento.getPromedioCalificaciones() : 0.0;

        long totalReservas = reservaRepositorio.countByAlojamiento_IdAndEstadoIn(
                id,
                List.of(ReservaEstado.CONFIRMADA, ReservaEstado.COMPLETADA)
        );

        return new MetricasDTO(totalResenas, promedioCalificaciones, totalReservas);
    }

    @Override
    public List<ItemAlojamientoDTO> obtenerAlojamientos(AlojamientoFiltroDTO filtros, int pagina) throws Exception {

        // Validaciones de filtros
        if (filtros.fechaEntrada() != null && filtros.fechaSalida() != null) {
            if (filtros.fechaEntrada().isAfter(filtros.fechaSalida())) {
                throw new ValidationException("La fecha de entrada no puede ser posterior a la fecha de salida");
            }

            if (filtros.fechaEntrada().isBefore(LocalDate.now())) {
                throw new ValidationException("La fecha de entrada no puede ser anterior a hoy");
            }
        }

        if (filtros.precioMin() != null && filtros.precioMax() != null) {
            if (filtros.precioMin() > filtros.precioMax()) {
                throw new ValidationException("El precio mínimo no puede ser mayor al precio máximo");
            }
        }

        if (filtros.huespedes() != null && filtros.huespedes() < 1) {
            throw new ValidationException("El número de huéspedes debe ser al menos 1");
        }

        // Crear paginación
        Pageable pageable = PageRequest.of(pagina, 10);

        // Buscar con filtros
        Page<ItemAlojamientoDTO> alojamientos = alojamientoRepositorio.buscarConFiltros(
                filtros.ciudad(),
                filtros.fechaEntrada(),
                filtros.fechaSalida(),
                filtros.huespedes(),
                filtros.precioMin(),
                filtros.precioMax(),
                Estado.ACTIVO,
                pageable
        ).map(alojamientoMapper::toItemDTO);


        return alojamientos.toList();
    }

    @Override
    public List<ItemAlojamientoDTO> obtenerAlojamientosUsuario(String id, int pagina) throws Exception {

        if(!authServicio.obtnerIdAutenticado(id)){
            throw new AccessDeniedException("No tiene permisos para ver los alojamientos de este usuario.");
        }

        Pageable pageable = PageRequest.of(pagina, 5);
        Page<ItemAlojamientoDTO> alojamientos = alojamientoRepositorio.getAlojamientos(id, Estado.ACTIVO ,pageable).map(alojamientoMapper::toItemDTO);

        return alojamientos.toList();
    }

    @Override
    public List<ItemAlojamientoDTO> sugerirAlojamientos(String ciudad){
        // Crear paginación para obtener los primeros 10 resultados
        Pageable pageable = PageRequest.of(0, 10);

        // Buscar alojamientos ordenados por calificación
        Page<ItemAlojamientoDTO> alojamientos = alojamientoRepositorio.sugerirPorCiudad(ciudad, Estado.ACTIVO, pageable).map(alojamientoMapper::toItemDTO);

        // Convertir a DTOs
        return alojamientos.toList();
    }

    public boolean existePorTitulo(String titulo){

        Optional<Alojamiento> optionalAlojamiento = alojamientoRepositorio.findByTitulo(titulo);

        return optionalAlojamiento.isPresent();
    }

    public Alojamiento obtenerAlojamientoId(Long id) throws Exception {
        Optional<Alojamiento> optionalAlojamiento = alojamientoRepositorio.findById(id);

        if(optionalAlojamiento.isEmpty()){
            throw new NoFoundException("No se encontro el alojamiento con el id: " + id);
        }

        return optionalAlojamiento.get();
    }

}