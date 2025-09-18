package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.PaginacionDTO;
import co.edu.uniquindio.application.dtos.alojamiento.ItemAlojamientoDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.usuario.EdicionUsuarioDTO;
import co.edu.uniquindio.application.dtos.usuario.UsuarioDTO;
import co.edu.uniquindio.application.dtos.usuario.CambioContrasenaDTO;


public interface UsuarioServicio {
    UsuarioDTO editarPerfil(Long id, EdicionUsuarioDTO dto) throws Exception;
    void eliminarCuenta(Long id) throws Exception;
    UsuarioDTO obtenerInformacion(Long id) throws Exception;
    void cambiarContrasena(Long id, CambioContrasenaDTO dto) throws Exception;
    PaginacionDTO<ItemAlojamientoDTO> listarAlojamientos(Long id, int pagina, int tamano) throws Exception;
    PaginacionDTO<ItemReservaDTO> listarReservas(Long id, int pagina, int tamano) throws Exception;
}