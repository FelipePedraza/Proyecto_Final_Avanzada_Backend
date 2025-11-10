package co.edu.uniquindio.application.services.impl;

import co.edu.uniquindio.application.dtos.EmailDTO;
import co.edu.uniquindio.application.services.EmailServicio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

import java.util.Collections;

@Service
public class EmailServicioImpl implements EmailServicio {

    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    @Value("${BREVO_FROM_EMAIL}")
    private String brevoFromEmail;

    @Value("${BREVO_FROM_NAME}")
    private String brevoFromName;

    @Override
    @Async
    public void enviarEmail(EmailDTO emailDTO) throws Exception {
        TransactionalEmailsApi api = new TransactionalEmailsApi();
        api.getApiClient().setApiKey(brevoApiKey);

        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setEmail(brevoFromEmail); // Tu email verificado en Brevo
        sender.setName(brevoFromName);

        SendSmtpEmailTo to = new SendSmtpEmailTo();
        to.setEmail(emailDTO.destinatario());

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.setSender(sender);
        sendSmtpEmail.setTo(Collections.singletonList(to));
        sendSmtpEmail.setSubject(emailDTO.sujeto());
        sendSmtpEmail.setTextContent(emailDTO.cuerpo());

        api.sendTransacEmail(sendSmtpEmail);
    }
}