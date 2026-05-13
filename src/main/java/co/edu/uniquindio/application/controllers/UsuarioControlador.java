package co.edu.uniquindio.application.controllers;


import co.edu.uniquindio.application.dtos.PageResponseDTO;
import co.edu.uniquindio.application.dtos.alojamiento.ItemAlojamientoDTO;
import co.edu.uniquindio.application.dtos.reserva.ItemReservaDTO;
import co.edu.uniquindio.application.dtos.usuario.*;
import co.edu.uniquindio.application.dtos.RespuestaDTO;
import co.edu.uniquindio.application.models.enums.ReservaEstado;
import co.edu.uniquindio.application.services.AlojamientoServicio;
import co.edu.uniquindio.application.services.ReservaServicio;
import co.edu.uniquindio.application.services.UsuarioServicio;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;
    private final AlojamientoServicio alojamientoServicio;
    private final ReservaServicio reservaServicio;

    @Timed(value = "vivigo.api.usuario", description = "Usuario API timing")
    @PostMapping("/anfitrion")
    public ResponseEntity<RespuestaDTO<String>> crearAnfitrion(@Valid @RequestBody CreacionAnfitrionDTO dto) throws Exception {
        usuarioServicio.crearAnfitrion(dto);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "Se ha creado el anfitrion"));
    }

    @Timed(value = "vivigo.api.usuario", description = "Usuario API timing")
    @PutMapping(value = "/{id}")
    public ResponseEntity<RespuestaDTO<String>> editar(@PathVariable String id, @RequestBody @Valid EdicionUsuarioDTO edicionUsuarioDTO) throws Exception {
        usuarioServicio.editar(id, edicionUsuarioDTO);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "El usuario ha sido actualizado"));
    }

    @Timed(value = "vivigo.api.usuario", description = "Usuario API timing")
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaDTO<UsuarioDTO>> obtener(@PathVariable String id) throws Exception {
        UsuarioDTO usuarioDTO = usuarioServicio.obtener(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, usuarioDTO));
    }

    @Timed(value = "vivigo.api.usuario", description = "Usuario API timing")
    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaDTO<String>> eliminar(@PathVariable String id) throws Exception {
        usuarioServicio.eliminar(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "El usuario ha sido eliminado"));
    }

    @Timed(value = "vivigo.api.usuario", description = "Usuario API timing")
    @PatchMapping("/{id}/contrasena")
    public ResponseEntity<RespuestaDTO<String>> cambiarContrasena(@PathVariable String id, @Valid @RequestBody CambioContrasenaDTO dto) throws Exception {
        usuarioServicio.cambiarContrasena(id, dto);
        return ResponseEntity.ok(new RespuestaDTO<>(false, "La contraseña ha sido cambiada"));
    }

    @Timed(value = "vivigo.api.usuario", description = "Usuario API timing")
    @GetMapping("/{id}/alojamientos")
    public ResponseEntity<RespuestaDTO<PageResponseDTO<ItemAlojamientoDTO>>> obtenerAlojamientosUsuario(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "creadoEn,desc") String[] sort) throws Exception {

        Sort sorting = Sort.by("creadoEn").descending();
        if (sort != null && sort.length > 0 && sort[0].contains(",")) {
            String[] sortParts = sort[0].split(",");
            String sortField = sortParts[0];
            String sortDirection = sortParts.length > 1 ? sortParts[1] : "desc";
            sorting = sortDirection.equalsIgnoreCase("asc")
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
        }

        Pageable pageable = PageRequest.of(page, size, sorting);
        PageResponseDTO<ItemAlojamientoDTO> alojamientos = alojamientoServicio.obtenerAlojamientosUsuario(id, pageable);
        return ResponseEntity.ok(new RespuestaDTO<>(false, alojamientos));
    }

    @Timed(value = "vivigo.api.usuario", description = "Usuario API timing")
    @GetMapping("/{id}/reservas")
    public ResponseEntity<RespuestaDTO<PageResponseDTO<ItemReservaDTO>>> obtenerReservasUsuario(
            @PathVariable String id,
            @RequestParam(required = false) ReservaEstado estado,
            @RequestParam(required = false) LocalDate fechaEntrada,
            @RequestParam(required = false) LocalDate fechaSalida,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "fechaEntrada,desc") String[] sort) throws Exception {

        Sort sorting = Sort.by("fechaEntrada").descending();
        if (sort != null && sort.length > 0 && sort[0].contains(",")) {
            String[] sortParts = sort[0].split(",");
            String sortField = sortParts[0];
            String sortDirection = sortParts.length > 1 ? sortParts[1] : "desc";
            sorting = sortDirection.equalsIgnoreCase("asc")
                    ? Sort.by(sortField).ascending()
                    : Sort.by(sortField).descending();
        }

        Pageable pageable = PageRequest.of(page, size, sorting);
        PageResponseDTO<ItemReservaDTO> reservas = reservaServicio.obtenerReservasUsuario(id, estado, fechaEntrada, fechaSalida, pageable);
        return ResponseEntity.ok(new RespuestaDTO<>(false, reservas));
    }

    @Timed(value = "vivigo.api.usuario", description = "Usuario API timing")
    @GetMapping("/{id}/anfitrion")
    public ResponseEntity<RespuestaDTO<AnfitrionPerfilDTO>> obtenerAnfitrion(@PathVariable String id) throws Exception {
        AnfitrionPerfilDTO anfitrionPerfilDTO = usuarioServicio.obtenerAnfitrion(id);
        return ResponseEntity.ok(new RespuestaDTO<>(false, anfitrionPerfilDTO));
    }

}
