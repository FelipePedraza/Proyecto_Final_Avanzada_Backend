package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.EmailDTO;
import co.edu.uniquindio.application.services.EmailServicio;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServicioImpl implements EmailServicio {

	// private final JavaMailSender mailSender;

	@Override
	public void enviarEmail(EmailDTO emailDTO) throws Exception {
		// Servicio de email no configurado. Metodo deshabilitado temporalmente.
		// Si se necesita enviar correos, toca descomentar la inyección y la lógica, y configurar spring.mail en application.properties.
	}
}
