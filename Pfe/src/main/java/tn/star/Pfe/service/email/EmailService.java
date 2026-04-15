package tn.star.Pfe.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService implements IEmailService {
    @Value("${spring.mail.host:#{null}}")
    private String mailHost;
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@example.com}")
    private String fromEmail;

    @Async
    public void sendWelcomeEmail(String to, String firstName) {
        if (mailSender == null) {
            log.warn("Mail not configured — skipping welcome email to: {}", to);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Bienvenue sur Amicale STAR");
            message.setText(String.format(
                    "Bonjour %s,\n\nVotre compte a été créé avec succès.\n\nCordialement,\nL'équipe Amicale STAR",
                    firstName
            ));
            mailSender.send(message);
            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    @Async
    public void sendAccountCreatedEmail(String to, String firstName, String temporaryPassword) {
        if (mailSender == null) {
            log.warn("Mail not configured — skipping account-created email to: {}", to);
            return;
        }
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
        }
    }

    public void sendPasswordResetEmail(String to, String tempPassword) {
        if ( mailHost== null || mailHost.isBlank()) {
            log.warn("Mail not configured — skipping password reset email");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Réinitialisation de votre mot de passe");
        message.setText(buildBody(tempPassword));

        mailSender.send(message);
        log.info("Password-reset e-mail sent to {}", to);
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