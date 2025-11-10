package co.edu.uniquindio.application.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class BrevoEmailDTO {

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

    // Constructor completo
    public BrevoEmailDTO(EmailContact sender, List<EmailContact> to, String subject, String htmlContent, String textContent) {
        this.sender = sender;
        this.to = to;
        this.subject = subject;
        this.htmlContent = htmlContent;
        this.textContent = textContent;
    }

    // Getters y Setters
    public EmailContact getSender() {
        return sender;
    }

    public void setSender(EmailContact sender) {
        this.sender = sender;
    }

    public List<EmailContact> getTo() {
        return to;
    }

    public void setTo(List<EmailContact> to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    // Clase interna para contactos
    public static class EmailContact {
        @JsonProperty("email")
        private String email;

        @JsonProperty("name")
        private String name;

        public EmailContact() {}

        public EmailContact(String email, String name) {
            this.email = email;
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}