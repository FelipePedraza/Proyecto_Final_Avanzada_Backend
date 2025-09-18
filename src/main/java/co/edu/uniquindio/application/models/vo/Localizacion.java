package co.edu.uniquindio.application.models.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Embeddable
public class Localizacion {

    @Column(nullable = false)
    private float latitud;

    @Column(nullable = false)
    private float longitud;
}
