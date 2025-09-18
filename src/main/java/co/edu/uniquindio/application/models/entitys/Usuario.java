package co.edu.uniquindio.application.models.entitys;

import co.edu.uniquindio.application.models.enums.Rol;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String contrasena;

    @Column(length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;


    private LocalDate fechaNacimiento;

    @Column(length = 300)
    private String foto;

    @OneToOne(mappedBy = "user")
    private PerfilAnfitrion perfilAnfitrion;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime creadoEn;

}