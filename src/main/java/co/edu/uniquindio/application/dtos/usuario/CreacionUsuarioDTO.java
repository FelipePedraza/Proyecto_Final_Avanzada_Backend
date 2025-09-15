package co.edu.uniquindio.application.dtos.usuario;

import java.time.LocalDate;

import org.hibernate.validator.constraints.Length;
import co.edu.uniquindio.application.models.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.hibernate.validator.constraints.URL;


public record CreacionUsuarioDTO(    
    @NotBlank @Length(max = 100) String nombre,
    @NotBlank @Length(max = 50) @Email String email,
    @NotBlank @Length(min = 8) String contrasena,
    @Length(max = 20) String telefono,        
    @URL String foto,
    @NotNull @Past LocalDate fechaNacimiento,
    @NotNull Rol rol
) {

}
