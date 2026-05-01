package tn.star.Pfe.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;
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
            log.info(" ", to);
        } catch (Exception e) {
            log.error("Failed to send account-created email to: {}", to, e);
        }
    }
}