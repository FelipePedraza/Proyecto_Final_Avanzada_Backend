package co.edu.uniquindio.application.services;

import co.edu.uniquindio.application.dtos.EmailDTO;

public interface EmailServicio {

    void enviarEmail(EmailDTO emailDTO) throws Exception;
}
