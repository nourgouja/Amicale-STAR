package tn.star.Pfe.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tn.star.Pfe.event.EmailFailedEvent;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Async
    public void sendAccountCreatedEmail(String to, String firstName, String temporaryPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Votre compte Amicale STAR a été créé");
            message.setText("""
                    Bonjour %s,

                    Un compte a été créé pour vous sur la plateforme Amicale STAR.

                    Vos identifiants de connexion :
                      - Email : %s
                      - Mot de passe temporaire : %s

                    Veuillez vous connecter et changer votre mot de passe dès votre première connexion.

                    Cordialement,
                    L'équipe Amicale STAR
                    """.formatted(firstName, to, temporaryPassword));
            mailSender.send(message);
            log.info("Account-created email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send account-created email to: {}", to, e);
            eventPublisher.publishEvent(new EmailFailedEvent(to, e.getMessage()));
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String tempPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Réinitialisation de votre mot de passe");
            message.setText(buildBody(tempPassword));
            mailSender.send(message);
            log.info("Password-reset e-mail sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send password-reset email to {}", to, e);
            eventPublisher.publishEvent(new EmailFailedEvent(to, e.getMessage()));
        }
    }

    @Override
    public void sendRejectionEmail(String to, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Décision de votre demande d'adhésion - AmicaleStar");
            message.setText("""
                    Bonjour %s,

                    Nous regrettons de vous informer que votre demande d'adhésion à AmicaleStar n'a pas été approuvée.

                    Si vous avez des questions, veuillez contacter l'administration.

                    Cordialement,
                    L'équipe Amicale STAR
                    """.formatted(firstName));
            mailSender.send(message);
            log.info("Rejection email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send rejection email to: {}", to, e);
            eventPublisher.publishEvent(new EmailFailedEvent(to, e.getMessage()));
        }
    }

    @Async
    @Override
    public void sendEcheanceReminder(String to, String firstName, String offreTitre,
                                      int numero, int total, BigDecimal montant,
                                      LocalDate dateEcheance, String type) {
        try {
            String subject = switch (type) {
                case "J_MOINS_7" -> "Rappel paiement — " + offreTitre + " (dans 7 jours)";
                case "J_MOINS_3" -> "Rappel paiement — " + offreTitre + " (dans 3 jours)";
                case "JOUR_J"    -> "Échéance aujourd'hui — " + offreTitre;
                case "J_PLUS_7"  -> "Paiement en retard — " + offreTitre;
                default          -> "Rappel de paiement — " + offreTitre;
            };

            String statusLine = switch (type) {
                case "J_MOINS_7" -> "Ce versement est dû dans 7 jours.";
                case "J_MOINS_3" -> "Ce versement est dû dans 3 jours.";
                case "JOUR_J"    -> "Ce versement est dû AUJOURD'HUI.";
                case "J_PLUS_7"  -> "Ce versement est EN RETARD de 7 jours. Veuillez régulariser dès que possible.";
                default          -> "";
            };

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText("""
                    Bonjour %s,

                    Rappel concernant votre inscription à "%s".

                    Versement %d/%d — Montant : %s DT
                    Date d'échéance : %s

                    %s

                    Pour tout règlement, veuillez contacter le bureau de l'Amicale STAR.

                    Cordialement,
                    L'équipe Amicale STAR
                    """.formatted(firstName, offreTitre, numero, total, montant, dateEcheance, statusLine));
            mailSender.send(message);
            log.info("Reminder email ({}) sent to {}", type, to);
        } catch (Exception e) {
            log.error("Failed to send reminder email to {}: {}", to, e.getMessage());
            eventPublisher.publishEvent(new EmailFailedEvent(to, e.getMessage()));
        }
    }

    private String buildBody(String tempPassword) {
        return """
                Bonjour,

                Votre mot de passe temporaire est : %s

                Connectez-vous et changez-le immédiatement.

                Cordialement,
                L'équipe Amicale STAR
                """.formatted(tempPassword);
    }
}
