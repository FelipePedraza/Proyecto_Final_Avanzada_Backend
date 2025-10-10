package co.edu.uniquindio.application.models.entitys;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario1_id", nullable = false)
    private Usuario usuario1;

    @ManyToOne
    @JoinColumn(name = "usuario2_id", nullable = false)
    private Usuario usuario2;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime creadoEn;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Mensaje> mensajes;

    @Column(nullable = false)
    private boolean activo;

    @PrePersist
    protected void onCreate() {
        creadoEn = LocalDateTime.now();
        activo = true;
    }
}
