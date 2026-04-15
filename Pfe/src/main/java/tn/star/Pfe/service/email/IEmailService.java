package tn.star.Pfe.service.email;

public interface IEmailService {
    void sendWelcomeEmail(String to, String firstName);
    void sendAccountCreatedEmail(String to, String firstName, String temporaryPassword);
    void sendPasswordResetEmail(String to, String temporaryPassword);
}
