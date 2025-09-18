package co.edu.uniquindio.application.dtos.alojamiento;

public record MetricasDTO(
        Integer numeroReservas,
        Float promedioCalificaciones,
        Double ingresosTotales,
        Float ocupacion
) {
}