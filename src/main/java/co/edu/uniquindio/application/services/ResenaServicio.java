package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.resena.CreacionResenaDTO;
import co.edu.uniquindio.application.dtos.resena.ItemResenaDTO;

import java.util.List;

public interface ResenaServicio {
    void crear(CreacionResenaDTO dto) throws Exception;
    List<ItemResenaDTO> listarPorAlojamiento(Long alojamientoId) throws Exception;
}
