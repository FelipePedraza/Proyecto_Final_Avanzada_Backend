package co.edu.uniquindio.application.dtos.alojamiento;

import co.edu.uniquindio.application.models.enums.Servicio;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

public record AlojamientoFiltroDTO (

        // Filtro por ciudad (búsqueda parcial, case-insensitive)
        String ciudad,

        // Filtro por disponibilidad en fechas específicas
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fechaEntrada,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fechaSalida,

        // Filtro por capacidad mínima de huéspedes
        @Min(value = 1, message = "El número de huéspedes debe ser al menos 1")
        Integer huespedes,

        // Filtro por rango de precios
        @Min(value = 0, message = "El precio mínimo no puede ser negativo")
        Float precioMin,

        @Min(value = 0, message = "El precio máximo no puede ser negativo")
        Float precioMax,

        List<Servicio> servicios
) {

}
