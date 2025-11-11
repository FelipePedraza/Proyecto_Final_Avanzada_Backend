package co.edu.uniquindio.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class BrevoEmailDTO {

    // Getters y Setters
    @JsonProperty("sender")
    private EmailContact sender;

    @JsonProperty("to")
    private List<EmailContact> to;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("htmlContent")
    private String htmlContent;

    @JsonProperty("textContent")
    private String textContent;

    // Constructor vacío
    public BrevoEmailDTO() {}

    // Clase interna para contactos
    @Setter
    @Getter
    public static class EmailContact {
        @JsonProperty("email")
        private String email;

        @JsonProperty("name")
        private String name;

        public EmailContact(String email, String name) {
            this.email = email;
            this.name = name;
        }

    }
}