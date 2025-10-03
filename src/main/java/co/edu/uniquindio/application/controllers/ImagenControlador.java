package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.services.ImagenServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/imagenes")
public class ImagenControlador {

    private final ImagenServicio imagenServicio;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<RespuestaDTO<Map>> actualizar(@RequestParam("file") MultipartFile image) throws Exception{
        Map respuesta = imagenServicio.actualizar(image, "Vivi_Go");
        return ResponseEntity.ok( new RespuestaDTO<>(false, respuesta) );
    }

    @DeleteMapping
    public ResponseEntity<RespuestaDTO<String>> eliminar(@RequestParam("id") String id) throws Exception{
        imagenServicio.eliminar(id);
        return ResponseEntity.ok( new RespuestaDTO<>(false, "Imagen eliminada exitosamente") );
    }
}
