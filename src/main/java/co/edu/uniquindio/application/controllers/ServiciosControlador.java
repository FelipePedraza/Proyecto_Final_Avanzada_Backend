package co.edu.uniquindio.application.controllers;

import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.models.enums.Servicio;
import co.edu.uniquindio.application.services.ServiciosServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServiciosControlador {

    private final ServiciosServicio serviciosServicio;

    @GetMapping
    public ResponseEntity<RespuestaDTO<List<Servicio>>> obtenerServicios() {
        List<Servicio> servicios = serviciosServicio.obtenerServicios();
        return ResponseEntity.ok(
                new RespuestaDTO<>(false, servicios)
        );
    }
}
