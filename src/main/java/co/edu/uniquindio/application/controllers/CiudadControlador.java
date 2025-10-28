package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.models.enums.Ciudad;
import co.edu.uniquindio.application.services.CiudadServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ciudades")
@RequiredArgsConstructor
public class CiudadControlador {

    private final CiudadServicio ciudadServicio;

    @GetMapping
    public ResponseEntity<RespuestaDTO<List<Ciudad>>> obtenerServicios() {
        List<Ciudad> ciudades = ciudadServicio.obtenerCiudades();
        return ResponseEntity.ok(
                new RespuestaDTO<>(false, ciudades)
        );
    }
}
