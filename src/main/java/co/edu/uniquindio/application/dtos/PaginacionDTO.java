package co.edu.uniquindio.application.dtos;

import java.util.List;

public record PaginacionDTO<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas,
        boolean esUltima,
        boolean esPrimera
) {
}
