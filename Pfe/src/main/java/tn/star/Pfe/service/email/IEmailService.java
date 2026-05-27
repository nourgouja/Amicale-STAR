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

    void sendElectionCallCreatedToAdmins(String to, String callTitre, String description);
    void sendCandidacyConfirmationToApplicant(String to, String firstName, String callTitre, String position);
    void sendNewCandidacyToAdmins(String to, String adminName, String applicantName, String callTitre, String position);
    void sendApplicationDecisionToApplicant(String to, String firstName, String callTitre, String position, boolean approved, String rejectionReason);
    void sendElectionOpenToVoter(String to, String firstName, String electionTitre, String startDate, String endDate);
    void sendResultsToAdmin(String to, String adminName, String electionTitre);
}
