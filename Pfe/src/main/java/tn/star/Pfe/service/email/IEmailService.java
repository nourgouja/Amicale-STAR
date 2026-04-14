package tn.star.Pfe.service.email;

public interface IEmailService {
    void sendWelcomeEmail(String to, String firstName);
    void sendPasswordResetEmail(String to, String temporaryPassword);
}
