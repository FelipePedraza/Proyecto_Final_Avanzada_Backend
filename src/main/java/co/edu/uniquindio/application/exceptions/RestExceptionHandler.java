package co.edu.uniquindio.application.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.dtos.ValidacionDTO;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RespuestaDTO<String>> noResourceFoundExceptionHandler(NoResourceFoundException ex){
        return ResponseEntity.status(404).body( new RespuestaDTO<>(true, "El recurso solicitado no existe") );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaDTO<String>> generalExceptionHandler (Exception e){
        return ResponseEntity.internalServerError().body( new RespuestaDTO<>(true, e.getMessage()) );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaDTO<List<ValidacionDTO>>> validationExceptionHandler ( MethodArgumentNotValidException ex ) {
        List<ValidacionDTO> errors = new ArrayList<>();
        BindingResult results = ex.getBindingResult();
        for (FieldError e: results.getFieldErrors()) {
            errors.add( new ValidacionDTO(e.getField(), e.getDefaultMessage()) );
        }
        return ResponseEntity.badRequest().body( new RespuestaDTO<>(true, errors) );
    }

    @ExceptionHandler(ValueConflictException.class)
    public ResponseEntity<RespuestaDTO<String>> handleValueConflictException(ValueConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body( new RespuestaDTO<>(true, ex.getMessage()) );
    }
}