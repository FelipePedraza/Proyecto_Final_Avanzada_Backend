package co.edu.uniquindio.application.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ImagenServicio {
    Map actualizar(MultipartFile image) throws Exception;
    Map eliminar(String imageId) throws Exception;
}
