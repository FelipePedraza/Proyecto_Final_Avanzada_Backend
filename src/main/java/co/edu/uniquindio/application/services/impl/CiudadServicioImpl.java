package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.models.enums.Ciudad;
import co.edu.uniquindio.application.services.CiudadServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CiudadServicioImpl implements CiudadServicio {

    @Override
    public List<Ciudad>obtenerCiudades() {
        return Arrays.asList(Ciudad.values());
    }

}
