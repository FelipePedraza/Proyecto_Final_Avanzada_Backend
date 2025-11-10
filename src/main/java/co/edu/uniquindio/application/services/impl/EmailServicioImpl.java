package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.EmailDTO;
import co.edu.uniquindio.application.services.EmailServicio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServicioImpl implements EmailServicio {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void enviarEmail(EmailDTO emailDTO) throws Exception {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(emailDTO.destinatario());
            message.setSubject(emailDTO.sujeto());
            message.setText(emailDTO.cuerpo());

            javaMailSender.send(message);

            log.info("Email enviado exitosamente a: {}", emailDTO.destinatario());
        } catch (Exception e) {
            log.error("Error al enviar email a {}: {}", emailDTO.destinatario(), e.getMessage());
            throw new Exception("Error al enviar el email: " + e.getMessage());
        }
    }
}