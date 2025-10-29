package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.services.ImagenServicio;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImagenServicioImpl implements ImagenServicio {

    private final Cloudinary cloudinary;

    public ImagenServicioImpl(){
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dxvvp4ote");
        config.put("api_key", "938831583477113");
        config.put("api_secret", "dGq2cGN1LhLybhLy0EK0FpX-BQ0");
        cloudinary = new Cloudinary(config);
    }

    @Override
    public Map actualizar(MultipartFile image, String carpeta) throws Exception {
        File file = convertir(image);
        return cloudinary.uploader().upload(file, ObjectUtils.asMap("folder", carpeta));
    }

    @Override
    public Map eliminar(String url) throws Exception {
        String imagenId = extraerPublicIdDelUrl(url);
        return cloudinary.uploader().destroy(imagenId, ObjectUtils.emptyMap());
    }

    private File convertir(MultipartFile imagen) throws IOException {
        File file = File.createTempFile(imagen.getOriginalFilename(), null);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(imagen.getBytes());
        fos.close();
        return file;
    }

    @Override
    public String extraerPublicIdDelUrl(String url) {
        try {
            // La URL de Cloudinary generalmente sigue este patrón:
            // https://res.cloudinary.com/<cloud_name>/image/upload/<version>/<public_id>.<format>

            // Buscar "upload/" en la URL
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex != -1) {
                String afterUpload = url.substring(uploadIndex + 8); // +8 para saltar "/upload/"

                // Remover la versión si existe (v1234567890/)
                if (afterUpload.startsWith("v")) {
                    int slashIndex = afterUpload.indexOf("/");
                    if (slashIndex != -1) {
                        afterUpload = afterUpload.substring(slashIndex + 1);
                    }
                }

                // Remover la extensión del archivo
                int lastDotIndex = afterUpload.lastIndexOf(".");
                if (lastDotIndex != -1) {
                    afterUpload = afterUpload.substring(0, lastDotIndex);
                }

                return afterUpload;
            }
        } catch (Exception e) {
            System.out.println("Error extrayendo public_id de URL: " + url);
        }
        return null;
    }
}