package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.usuario.CreacionAnfitrionDTO;
import co.edu.uniquindio.application.dtos.usuario.AnfitrionPerfilDTO;
import co.edu.uniquindio.application.mappers.PerfilAnfitrionMapper;
import co.edu.uniquindio.application.models.entitys.PerfilAnfitrion;
import co.edu.uniquindio.application.repositories.PerfilAnfitrionRepositorio;
import co.edu.uniquindio.application.services.PerfilAnfitrionServicio;
import org.springframework.stereotype.Service;

@Service
public class PerfilAnfitrionServicioImpl implements PerfilAnfitrionServicio {

    private final PerfilAnfitrionRepositorio perfilAnfitrionRepositorio;
    private final PerfilAnfitrionMapper perfilAnfitrionMapper;

    public PerfilAnfitrionServicioImpl(PerfilAnfitrionRepositorio perfilAnfitrionRepositorio, PerfilAnfitrionMapper perfilAnfitrionMapper) {
        this.perfilAnfitrionRepositorio = perfilAnfitrionRepositorio;
        this.perfilAnfitrionMapper = perfilAnfitrionMapper;
    }

    @Override
    public void crear(CreacionAnfitrionDTO dto) throws Exception {
        PerfilAnfitrion entity = perfilAnfitrionMapper.toEntity(dto);
        perfilAnfitrionRepositorio.save(entity);
    }

    @Override
    public AnfitrionPerfilDTO obtener(Long id) throws Exception {
        PerfilAnfitrion entity = perfilAnfitrionRepositorio.findById(id)
                .orElseThrow(() -> new Exception("Perfil no encontrado"));
        return perfilAnfitrionMapper.toDTO(entity);
    }
}
