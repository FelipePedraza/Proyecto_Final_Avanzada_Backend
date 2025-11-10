package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.BrevoEmailDTO;
import co.edu.uniquindio.application.dtos.EmailDTO;
import co.edu.uniquindio.application.services.EmailServicio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServicioImpl implements EmailServicio {

    private final RestTemplate restTemplate;

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.api.url}")
    private String apiUrl;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    @Override
    @Async
    public void enviarEmail(EmailDTO emailDTO) throws Exception {
        try {
            // Crear el objeto para Brevo
            BrevoEmailDTO brevoEmail = new BrevoEmailDTO();

            // Configurar remitente
            brevoEmail.setSender(new BrevoEmailDTO.EmailContact(senderEmail, senderName));

            // Configurar destinatario
            brevoEmail.setTo(List.of(
                    new BrevoEmailDTO.EmailContact(emailDTO.destinatario(), emailDTO.destinatario())
            ));

            // Configurar asunto
            brevoEmail.setSubject(emailDTO.sujeto());

            // Configurar contenido (texto plano y HTML)
            brevoEmail.setTextContent(emailDTO.cuerpo());
            brevoEmail.setHtmlContent(convertirTextoAHtml(emailDTO.cuerpo()));

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);
            headers.set("accept", "application/json");

            // Crear la petición
            HttpEntity<BrevoEmailDTO> request = new HttpEntity<>(brevoEmail, headers);

            // Enviar la petición
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email enviado exitosamente a: {} vía Brevo", emailDTO.destinatario());
            } else {
                log.error("Error al enviar email. Status: {}, Response: {}",
                        response.getStatusCode(), response.getBody());
                throw new Exception("Error al enviar email: " + response.getBody());
            }

        } catch (Exception e) {
            log.error("Error al enviar email a {}: {}", emailDTO.destinatario(), e.getMessage());
            throw new Exception("Error al enviar el email: " + e.getMessage());
        }
    }

    /**
     * Convierte texto plano a HTML básico para mejor visualización
     */
    private String convertirTextoAHtml(String texto) {
        if (texto == null) {
            return "";
        }

        // Reemplazar saltos de línea por <br>
        String html = texto.replace("\n", "<br>");

        // Envolver en estructura HTML básica
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                    }
                </style>
            </head>
            <body>
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    %s
                </div>
            </body>
            </html>
            """.formatted(html);
    }
}