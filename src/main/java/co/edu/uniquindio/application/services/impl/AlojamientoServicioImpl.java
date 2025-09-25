package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.alojamiento.*;
import co.edu.uniquindio.application.dtos.resena.CreacionResenaDTO;
import co.edu.uniquindio.application.dtos.resena.ItemResenaDTO;
import co.edu.uniquindio.application.dtos.usuario.EdicionUsuarioDTO;
import co.edu.uniquindio.application.models.entitys.Alojamiento;
import co.edu.uniquindio.application.repositories.AlojamientoRepositorio;
import co.edu.uniquindio.application.services.AlojamientoServicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlojamientoServicioImpl implements AlojamientoServicio {

    private AlojamientoRepositorio alojamientoRepositorio;

    @Override
    public void crear(CreacionAlojamientoDTO alojamientoDTO) throws Exception {

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
}