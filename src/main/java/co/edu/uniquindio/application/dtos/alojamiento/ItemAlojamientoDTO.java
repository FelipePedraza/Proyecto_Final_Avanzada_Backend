package co.edu.uniquindio.application.dtos.alojamiento;

public record ItemAlojamientoDTO(
        Long id,
        String titulo,
        String imagenPrincipal,
        Double precioPorNoche,
        DireccionDTO direccion,
        Double promedioCalificaciones
) {
}