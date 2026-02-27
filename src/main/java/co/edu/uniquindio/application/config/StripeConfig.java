package co.edu.uniquindio.application.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.currency}")
    private String currency;

    /**
     * Inicializa la clave secreta globalmente en el SDK de Stripe.
     * Todos los llamados al API de Stripe la usan automáticamente.
     */
    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
}
