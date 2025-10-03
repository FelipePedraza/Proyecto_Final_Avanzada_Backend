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
    public Map actualizar(MultipartFile image) throws Exception {
        File file = convertir(image);
        return cloudinary.uploader().upload(file, ObjectUtils.asMap("folder", "Vivi_Go"));
    }

    @Override
    public Map eliminar(String imagenId) throws Exception {
        return cloudinary.uploader().destroy(imagenId, ObjectUtils.emptyMap());
    }

    private File convertir(MultipartFile imagen) throws IOException {
        File file = File.createTempFile(imagen.getOriginalFilename(), null);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(imagen.getBytes());
        fos.close();
        return file;
    }
}