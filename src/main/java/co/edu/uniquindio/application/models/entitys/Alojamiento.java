package co.edu.uniquindio.application.models.entitys;


import co.edu.uniquindio.application.models.enums.Estado;
import co.edu.uniquindio.application.models.enums.Servicio;
import co.edu.uniquindio.application.models.vo.Direccion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Alojamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false,  length = 2000)
    private String descripcion;

    @Embedded
    @Column(nullable = false)
    private Direccion direccion;

    @Column(nullable = false,  length = 3)
    private Integer maxHuespedes;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime creadoEn;

    @Column(nullable = false,  length = 20)
    private Float precioPorNoche;

    @ElementCollection
    @Column(name = "imagen_url", length = 500, nullable = false)
    private List<String> imagenes;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    @ElementCollection
    private Set<Servicio> servicios;

    @ManyToOne
    @JoinColumn(name = "anfitrion_id", nullable = false)
    private Usuario anfitrion;

    @OneToMany(mappedBy = "alojamiento")
    private List<Reserva> reservas;

    @OneToMany(mappedBy = "alojamiento")
    private List<Resena> resenas;

}
