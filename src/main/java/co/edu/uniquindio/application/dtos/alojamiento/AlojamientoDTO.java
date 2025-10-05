package co.edu.uniquindio.application.dtos.alojamiento;

import co.edu.uniquindio.application.models.enums.Servicio;

import java.util.List;

public record AlojamientoDTO(

        Long id,
        
        String titulo,

        String descripcion,

        DireccionDTO direccion,

        Float precioPorNoche,

        Integer maxHuespedes,

        List<Servicio> servicios,

        List<String> imagenes,

        String nombreAnfitrion
) {
}