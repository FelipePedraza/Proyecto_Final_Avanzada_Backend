package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.models.enums.Servicio;
import co.edu.uniquindio.application.services.ServiciosServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiciosServicioImpl implements ServiciosServicio {

    @Override
    public List<Servicio> obtenerServicios() {
        return Arrays.asList(Servicio.values());
    }

}
