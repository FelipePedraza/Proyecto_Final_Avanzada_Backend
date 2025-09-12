package co.edu.uniquindio.application.dtos;

/**
 * DTO uniformador de respuestas según la guía.
 * @param error indica si hubo error (true) o no (false)
 * @param data  payload de la respuesta o detalles del error
 * @param <T> tipo del payload
 */
public record RespuestaDTO<T>(
    boolean error, 
    T data
) {

}