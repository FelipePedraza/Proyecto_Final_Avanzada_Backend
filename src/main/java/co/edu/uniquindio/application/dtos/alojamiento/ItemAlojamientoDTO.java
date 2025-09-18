package co.edu.uniquindio.application.dtos.alojamiento;

public record ItemAlojamientoDTO(
        Long id,
        String titulo,
        String imagenPrincipal,
        Float precioNoche,
        DireccionDTO ubicacion,
        Float promedioCalificaciones,
        Integer capacidad
) {
}