package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.alojamiento.ItemAlojamientoDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.usuario.CambioContrasenaDTO;
import co.edu.uniquindio.application.dtos.usuario.EdicionUsuarioDTO;
import co.edu.uniquindio.application.dtos.usuario.UsuarioDTO;
import co.edu.uniquindio.application.services.UsuarioServicio;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServicioImpl implements UsuarioServicio {


    @Override
    public UsuarioDTO editarPerfil(Long id, EdicionUsuarioDTO dto) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public void eliminarCuenta(Long id) throws Exception {
        // Lógica de negocio a implementar
    }

    @Override
    public UsuarioDTO obtenerInformacion(Long id) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public void cambiarContrasena(Long id, CambioContrasenaDTO dto) throws Exception {
        // Lógica de negocio a implementar
    }

    @Override
    public PaginacionDTO<ItemAlojamientoDTO> listarAlojamientos(Long id, int pagina, int tamano) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }

    @Override
    public PaginacionDTO<ItemReservaDTO> listarReservas(Long id, int pagina, int tamano) throws Exception {
        // Lógica de negocio a implementar
        return null;
    }
}
