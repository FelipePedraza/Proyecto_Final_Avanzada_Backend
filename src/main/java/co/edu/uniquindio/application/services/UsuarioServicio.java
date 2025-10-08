package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.alojamiento.ItemAlojamientoDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.usuario.*;
import co.edu.uniquindio.application.models.enums.ReservaEstado;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;


public interface UsuarioServicio {
    void crear(CreacionUsuarioDTO dto) throws Exception;
    void editar(String id, EdicionUsuarioDTO dto, MultipartFile file) throws Exception;
    void eliminar(String id) throws Exception;
    UsuarioDTO obtener(String id) throws Exception;
    void cambiarContrasena(String id, CambioContrasenaDTO dto) throws Exception;
    void reiniciarContrasena(ReinicioContrasenaDTO dto) throws Exception;
    void crearAnfitrion(CreacionAnfitrionDTO dto) throws Exception;
    List<ItemAlojamientoDTO> obtenerAlojamientosUsuario(String id, int pagina) throws Exception;
    List<ItemReservaDTO> obtenerReservasUsuario(String id, ReservaEstado estado, LocalDate fechaEntrada, LocalDate fechaSalida, int pagina) throws Exception;
}
