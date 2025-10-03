package co.edu.uniquindio.application.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ImagenServicio {
    Map<String, Object> actualizar(MultipartFile image, String carpeta) throws Exception;
    Map<String, Object> eliminar(String imageId) throws Exception;
    String extraerPublicIdDelUrl(String fotoUrl);
}
