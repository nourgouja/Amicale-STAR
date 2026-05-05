package tn.star.Pfe.service.email;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface IEmailService {
    void sendAccountCreatedEmail(String to, String firstName, String temporaryPassword);
    void sendPasswordResetEmail(String to, String temporaryPassword);
    void sendRejectionEmail(String to, String firstName);
    void sendEcheanceReminder(String to, String firstName, String offreTitre,
                               int numero, int total, BigDecimal montant,
                               LocalDate dateEcheance, String type);
}
