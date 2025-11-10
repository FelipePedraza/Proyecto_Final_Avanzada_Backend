package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.EmailDTO;
import co.edu.uniquindio.application.services.EmailServicio;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EmailServicioImpl implements EmailServicio {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Async
    public void enviarEmail(EmailDTO emailDTO) throws Exception {
        try {
            log.info("Enviando email a: {} con asunto: {}",
                    emailDTO.destinatario(), emailDTO.sujeto());

            // Crear payload para Resend
            Map<String, Object> payload = new HashMap<>();
            payload.put("from", fromEmail);
            payload.put("to", new String[]{emailDTO.destinatario()});
            payload.put("subject", emailDTO.sujeto());
            payload.put("text", emailDTO.cuerpo());

            String jsonPayload = objectMapper.writeValueAsString(payload);

            // Crear request HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // Enviar request
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // Verificar respuesta
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Email enviado exitosamente a: {} | Status: {}",
                        emailDTO.destinatario(), response.statusCode());
            } else {
                log.error("Error al enviar email. Status: {}, Body: {}",
                        response.statusCode(), response.body());
                throw new Exception("Error al enviar email: HTTP " + response.statusCode());
            }

        } catch (Exception ex) {
            log.error("Error al enviar email a {}: {}",
                    emailDTO.destinatario(), ex.getMessage());
            throw new Exception("Error al enviar email", ex);
        }
    }
}