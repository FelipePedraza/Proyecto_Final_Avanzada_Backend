package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.resena.CreacionResenaDTO;
import co.edu.uniquindio.application.dtos.resena.CreacionRespuestaDTO;
import co.edu.uniquindio.application.dtos.resena.ItemResenaDTO;

import java.util.List;

public interface ResenaServicio {

    //cada vez que se cree una reseña, actualiza el promedio del alojamiento
    void crear(CreacionResenaDTO dto) throws Exception;

    void responder(Long resenaId, CreacionRespuestaDTO dto) throws Exception;

    List<ItemResenaDTO> listarResenasAlojamiento(Long alojamientoId, int pagina) throws Exception;

}
