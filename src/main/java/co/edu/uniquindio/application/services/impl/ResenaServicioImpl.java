package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.resena.CreacionResenaDTO;
import co.edu.uniquindio.application.dtos.resena.ItemResenaDTO;
import co.edu.uniquindio.application.mappers.ResenaMapper;
import co.edu.uniquindio.application.models.entitys.Resena;
import co.edu.uniquindio.application.repositories.ResenaRepositorio;
import co.edu.uniquindio.application.services.ResenaServicio;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResenaServicioImpl implements ResenaServicio {

    private final ResenaRepositorio resenaRepositorio;
    private final ResenaMapper resenaMapper;

    public ResenaServicioImpl(ResenaRepositorio resenaRepositorio, ResenaMapper resenaMapper) {
        this.resenaRepositorio = resenaRepositorio;
        this.resenaMapper = resenaMapper;
    }

    @Override
    public void crear(CreacionResenaDTO dto) throws Exception {
        Resena entity = resenaMapper.toEntity(dto);
        resenaRepositorio.save(entity);
    }

    @Override
    public List<ItemResenaDTO> listarPorAlojamiento(Long alojamientoId) throws Exception {
        List<Resena> resenas = resenaRepositorio.findByAlojamientoId(alojamientoId);
        return resenas.stream()
                .map(resenaMapper::toItemDTO)
                .toList();
    }
}
