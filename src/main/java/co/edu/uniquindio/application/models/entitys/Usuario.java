package co.edu.uniquindio.application.models.entitys;

import co.edu.uniquindio.application.models.enums.Rol;
import co.edu.uniquindio.application.models.enums.Estado;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {

    @Id
    private String id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 150)
    private String contrasena;

    @Column(nullable = false, length = 15)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Column(length = 300)
    private String foto;

    @OneToOne(mappedBy = "usuario")
    private PerfilAnfitrion perfilAnfitrion;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime creadoEn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    private Boolean esAnfitrion;


}