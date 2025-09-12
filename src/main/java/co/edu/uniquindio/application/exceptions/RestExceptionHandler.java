package co.edu.uniquindio.application.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.ValidacionDTO;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<RespuestaDTO<String>> handleNotFound(ResourceNotFoundException ex){
        return ResponseEntity.status(404).body(new RespuestaDTO<>(true, "El recurso solicitado no existe"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaDTO<List<ValidacionDTO>>> handleValidation(MethodArgumentNotValidException ex) {
        List<ValidacionDTO> errors = new ArrayList<>();
        BindingResult results = ex.getBindingResult();
        for (FieldError f : results.getFieldErrors()) {
            errors.add(new ValidacionDTO(f.getField(), f.getDefaultMessage()));
        }
        return ResponseEntity.badRequest().body(new RespuestaDTO<>(true, errors));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaDTO<String>> handleConflict(DataIntegrityViolationException ex) {
        // Mensaje genérico o extraer causa para el detalle
        return ResponseEntity.status(409).body(new RespuestaDTO<>(true, "Conflicto en la operación: " + ex.getMostSpecificCause().getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaDTO<String>> handleGeneral(Exception ex) {
        ex.printStackTrace(); // opcional: registrar el stacktrace
        return ResponseEntity.internalServerError().body(new RespuestaDTO<>(true, ex.getMessage()));
    }
}