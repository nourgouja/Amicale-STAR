package tn.star.Pfe.service.authentification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
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

    public void sendPasswordResetEmail(String to, String temporaryPassword) {
        if (mailSender == null) {
            log.warn("Mail not configured — skipping password reset email to: {}", to);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Réinitialisation de votre mot de passe");
            message.setText(String.format(
                    "Bonjour,\n\nVotre mot de passe temporaire est : %s\n\nConnectez-vous et changez-le immédiatement.\n\nCordialement,\nL'équipe Amicale STAR",
                    temporaryPassword
            ));
            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }
}