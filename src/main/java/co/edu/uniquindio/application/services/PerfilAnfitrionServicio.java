package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.usuario.CreacionAnfitrionDTO;
import co.edu.uniquindio.application.dtos.usuario.AnfitrionPerfilDTO;

public interface PerfilAnfitrionServicio {
    void crear(CreacionAnfitrionDTO dto) throws Exception;
    AnfitrionPerfilDTO obtener(Long id) throws Exception;
}
