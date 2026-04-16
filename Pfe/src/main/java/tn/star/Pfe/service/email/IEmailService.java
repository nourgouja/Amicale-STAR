package tn.star.Pfe.service.email;

public interface IEmailService {
    void sendAccountCreatedEmail(String to, String firstName, String temporaryPassword);
    void sendPasswordResetEmail(String to, String temporaryPassword);
}
