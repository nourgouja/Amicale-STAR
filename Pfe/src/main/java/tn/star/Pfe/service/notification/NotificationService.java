package tn.star.Pfe.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tn.star.Pfe.dto.notification.NotificationDto;
import tn.star.Pfe.dto.notification.NotificationDto.Severity;
import tn.star.Pfe.entity.MembreBureau;
import tn.star.Pfe.enums.Role;
import tn.star.Pfe.event.*;
import tn.star.Pfe.repository.UserRepository;
import tn.star.Pfe.service.email.IEmailService;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationStore store;
    private final UserRepository userRepository;
    private final IEmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAdhesionDemande(AdhesionDemandeEvent event) {
        try {
            var a = event.adherent();
            log.info("Processing adhesion request from {} ({})", a.getPrenom() + " " + a.getNom(), a.getMatriculeStar());

            var admins = userRepository.findByRole(Role.ADMIN);
            log.debug("Found {} admin(s) to notify", admins.size());

            admins.forEach(admin -> {
                try {
                    store.push(admin.getId(), new NotificationDto(
                            UUID.randomUUID().toString(),
                            "ADHESION_DEMANDE",
                            "New adhesion request from " + a.getPrenom() + " " + a.getNom() + " (" + a.getMatriculeStar() + ")",
                            "/admin/utilisateurs",
                            NotificationDto.Severity.INFO,
                            LocalDateTime.now()
                    ));
                    log.debug("Notification sent to admin with ID: {}", admin.getId());
                } catch (Exception e) {
                    log.error("Failed to send notification to admin {}", admin.getId(), e);
                }
            });

            log.info("Adhesion request notification sent successfully");
        } catch (Exception e) {
            log.error("Error processing adhesion demand event", e);
        }
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEcheanceOverdue(EcheanceOverdueEvent event) {
        try {
            var echeance = event.echeance();
            var adherent = echeance.getInscription().getAdherent();
            var offre = echeance.getInscription().getOffre();

            log.info("Processing overdue echeance for adherent {} ({})", adherent.getPrenom() + " " + adherent.getNom(), adherent.getId());

            store.push(adherent.getId(), new NotificationDto(
                    UUID.randomUUID().toString(),
                    "ECHEANCE_OVERDUE",
                    "OVERDUE: Payment of " + echeance.getMontant() + " DT for \"" + offre.getTitre() + "\" was due " + echeance.getDateEcheance(),
                    "/home",
                    Severity.ERROR,
                    LocalDateTime.now()
            ));

            var membreBureau = userRepository.findByRole(Role.MEMBRE_BUREAU);
            log.debug("Found {} bureau member(s) to notify", membreBureau.size());

            membreBureau.forEach(u -> {
                if (u instanceof MembreBureau) {
                    try {
                        store.push(u.getId(), new NotificationDto(
                                UUID.randomUUID().toString(),
                                "MEMBRE_ECHEANCE_OVERDUE",
                                adherent.getPrenom() + " " + adherent.getNom() + " has an overdue payment of " + echeance.getMontant() + " DT on \"" + offre.getTitre() + "\"",
                                "/home",
                                Severity.WARNING,
                                LocalDateTime.now()
                        ));
                        log.debug("Overdue notification sent to bureau member {}", u.getId());
                    } catch (Exception e) {
                        log.error("Failed to send overdue notification to bureau member {}", u.getId(), e);
                    }
                }
            });

            log.info("Overdue echeance notifications sent successfully");
        } catch (Exception e) {
            log.error("Error processing overdue echeance event", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmailFailed(EmailFailedEvent event) {
        try {
            log.warn("Email delivery failed for {}: {}", event.recipientEmail(), event.reason());

            var admins = userRepository.findByRole(Role.ADMIN);
            log.debug("Found {} admin(s) to notify of email failure", admins.size());

            admins.forEach(admin -> {
                try {
                    store.push(admin.getId(), new NotificationDto(
                            UUID.randomUUID().toString(),
                            "EMAIL_FAILED",
                            "Email delivery failed for " + event.recipientEmail() + ": " + event.reason(),
                            null,
                            Severity.ERROR,
                            LocalDateTime.now()
                    ));
                    log.debug("Email failure notification sent to admin {}", admin.getId());
                } catch (Exception e) {
                    log.error("Failed to send email failure notification to admin {}", admin.getId(), e);
                }
            });

            log.info("Email failure notifications sent successfully");
        } catch (Exception e) {
            log.error("Error processing email failed event", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOffreCreated(OffreCreatedEvent event) {
        try {
            log.info("Processing new offer created: {}", event.offre().getTitre());

            var adherents = userRepository.findByRole(Role.ADHERENT);
            log.debug("Found {} adherent(s) to notify of new offer", adherents.size());

            adherents.forEach(u -> {
                try {
                    store.push(u.getId(), new NotificationDto(
                            UUID.randomUUID().toString(),
                            "NOUVELLE_OFFRE",
                            "New offer available: \"" + event.offre().getTitre() + "\"",
                            "/home",
                            Severity.INFO,
                            LocalDateTime.now()
                    ));
                    log.debug("New offer notification sent to adherent {}", u.getId());
                } catch (Exception e) {
                    log.error("Failed to send new offer notification to adherent {}", u.getId(), e);
                }
            });

            log.info("New offer notifications sent successfully");
        } catch (Exception e) {
            log.error("Error processing offer created event", e);
        }
    }
}
