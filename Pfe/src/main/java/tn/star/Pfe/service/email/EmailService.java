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

    @Async
    @Override
    public void sendElectionCallCreatedToAdmins(String to, String callTitre, String description) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Nouvel appel à candidature : " + callTitre);
            message.setText("""
                    Bonjour,

                    Un nouvel appel à candidature a été créé sur la plateforme Amicale STAR.

                    Titre : %s
                    Description : %s

                    Veuillez vous connecter à l'administration pour consulter les détails.

                    Cordialement,
                    L'équipe Amicale STAR
                    """.formatted(callTitre, description));
            mailSender.send(message);
            log.info("Election call created email sent to admin: {}", to);
        } catch (Exception e) {
            log.error("Failed to send election call created email to: {}", to, e);
            eventPublisher.publishEvent(new EmailFailedEvent(to, e.getMessage()));
        }
    }

    @Async
    @Override
    public void sendCandidacyConfirmationToApplicant(String to, String firstName, String callTitre, String position) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Confirmation de votre candidature – " + callTitre);
            message.setText("""
                    Bonjour %s,

                    Votre candidature pour le poste de %s dans le cadre de l'appel "%s" a bien été reçue.

                    Votre dossier sera examiné par l'administration dans les meilleurs délais.
                    Vous serez informé(e) de la décision par email.

                    Cordialement,
                    L'équipe Amicale STAR
                    """.formatted(firstName, position, callTitre));
            mailSender.send(message);
            log.info("Candidacy confirmation email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send candidacy confirmation email to: {}", to, e);
            eventPublisher.publishEvent(new EmailFailedEvent(to, e.getMessage()));
        }
    }

    @Async
    @Override
    public void sendNewCandidacyToAdmins(String to, String adminName, String applicantName, String callTitre, String position) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Nouvelle candidature reçue – " + callTitre);
            message.setText("""
                    Bonjour %s,

                    Une nouvelle candidature a été soumise pour l'appel "%s".

                    Candidat : %s
                    Poste sollicité : %s

                    Connectez-vous à l'administration pour examiner le dossier.

                    Cordialement,
                    L'équipe Amicale STAR
                    """.formatted(adminName, callTitre, applicantName, position));
            mailSender.send(message);
            log.info("New candidacy admin notification sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send new candidacy admin email to: {}", to, e);
            eventPublisher.publishEvent(new EmailFailedEvent(to, e.getMessage()));
        }
    }

    @Async
    @Override
    public void sendApplicationDecisionToApplicant(String to, String firstName, String callTitre, String position, boolean approved, String rejectionReason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            if (approved) {
                message.setSubject("Candidature approuvée – " + callTitre);
                message.setText("""
                        Bonjour %s,

                        Félicitations ! Votre candidature pour le poste de %s dans le cadre de l'appel "%s" a été APPROUVÉE.

                        Vous figurerez parmi les candidats officiels lors du scrutin.

                        Cordialement,
                        L'équipe Amicale STAR
                        """.formatted(firstName, position, callTitre));
            } else {
                message.setSubject("Candidature non retenue – " + callTitre);
                message.setText("""
                        Bonjour %s,

                        Nous regrettons de vous informer que votre candidature pour le poste de %s dans le cadre de l'appel "%s" n'a pas été retenue.

                        %s

                        Pour toute question, veuillez contacter l'administration.

                        Cordialement,
                        L'équipe Amicale STAR
                        """.formatted(firstName, position, callTitre,
                        rejectionReason != null && !rejectionReason.isBlank()
                                ? "Motif : " + rejectionReason
                                : ""));
            }
            mailSender.send(message);
            log.info("Application decision email sent to: {} (approved={})", to, approved);
        } catch (Exception e) {
            log.error("Failed to send application decision email to: {}", to, e);
            eventPublisher.publishEvent(new EmailFailedEvent(to, e.getMessage()));
        }
    }

    @Async
    @Override
    public void sendElectionOpenToVoter(String to, String firstName, String electionTitre, String startDate, String endDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Scrutin ouvert : " + electionTitre);
            message.setText("""
                    Bonjour %s,

                    L'élection "%s" est maintenant ouverte au vote.

                    Période de vote : du %s au %s

                    Connectez-vous à la plateforme Amicale STAR pour consulter les candidats et voter.

                    Cordialement,
                    L'équipe Amicale STAR
                    """.formatted(firstName, electionTitre, startDate, endDate));
            mailSender.send(message);
            log.info("Election open notification sent to voter: {}", to);
        } catch (Exception e) {
            log.error("Failed to send election open email to: {}", to, e);
            eventPublisher.publishEvent(new EmailFailedEvent(to, e.getMessage()));
        }
    }

    @Async
    @Override
    public void sendResultsToAdmin(String to, String adminName, String electionTitre) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Résultats publiés : " + electionTitre);
            message.setText("""
                    Bonjour %s,

                    Les résultats de l'élection "%s" ont été publiés sur la plateforme Amicale STAR.

                    Connectez-vous au tableau de bord administrateur pour consulter et archiver les résultats.

                    Cordialement,
                    L'équipe Amicale STAR
                    """.formatted(adminName, electionTitre));
            mailSender.send(message);
            log.info("Results published email sent to admin: {}", to);
        } catch (Exception e) {
            log.error("Failed to send results email to: {}", to, e);
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
