package tn.star.Pfe.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tn.star.Pfe.dto.notification.NotificationDto;
import tn.star.Pfe.dto.notification.NotificationDto.Severity;
import tn.star.Pfe.entity.election.CandidateApplication;
import tn.star.Pfe.entity.user.MembreBureau;
import tn.star.Pfe.entity.user.User;
import tn.star.Pfe.enums.ApprovalStatus;
import tn.star.Pfe.enums.Role;
import tn.star.Pfe.event.*;
import tn.star.Pfe.repository.user.UserRepository;
import tn.star.Pfe.service.email.IEmailService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInscriptionCreee(InscriptionCreeeEvent event) {
        try {
            var ins      = event.inscription();
            var adherent = ins.getAdherent();
            var offre    = ins.getOffre();

            userRepository.findByRole(Role.MEMBRE_BUREAU).forEach(u -> {
                try {
                    store.push(u.getId(), new NotificationDto(
                            UUID.randomUUID().toString(),
                            "INSCRIPTION_CREEE",
                            adherent.getPrenom() + " " + adherent.getNom()
                                    + " vient de s'inscrire à \"" + offre.getTitre() + "\"",
                            "/bureau/inscriptions",
                            Severity.INFO,
                            LocalDateTime.now()
                    ));
                } catch (Exception e) {
                    log.error("Failed to notify bureau member {} of new inscription", u.getId(), e);
                }
            });
        } catch (Exception e) {
            log.error("Error processing InscriptionCreeeEvent", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInscriptionStatusChanged(InscriptionStatusChangedEvent event) {
        try {
            var ins      = event.inscription();
            var adherent = ins.getAdherent();
            var offre    = ins.getOffre();
            var newStatut = event.newStatut();

            if (newStatut == ApprovalStatus.APPROVED) {
                store.push(adherent.getId(), new NotificationDto(
                        UUID.randomUUID().toString(),
                        "INSCRIPTION_CONFIRMEE",
                        "Votre inscription à \"" + offre.getTitre() + "\" a été confirmée !",
                        "/adherent/inscriptions",
                        Severity.SUCCESS,
                        LocalDateTime.now()
                ));
            } else if (newStatut == ApprovalStatus.REJECTED) {
                store.push(adherent.getId(), new NotificationDto(
                        UUID.randomUUID().toString(),
                        "INSCRIPTION_REJETEE",
                        "Votre inscription à \"" + offre.getTitre() + "\" a été refusée.",
                        "/adherent/offres",
                        Severity.ERROR,
                        LocalDateTime.now()
                ));
            }
        } catch (Exception e) {
            log.error("Error processing InscriptionStatusChangedEvent", e);
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


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onElectionCallCreated(ElectionCallCreatedEvent event) {
        try {
            String msg = "Nouvel appel à candidature : «" + event.call().getTitre() + "»";
            userRepository.findByRole(Role.ADMIN).forEach(admin -> {
                try {
                    store.push(admin.getId(), new NotificationDto(
                            UUID.randomUUID().toString(),
                            "ELECTION_CALL_CREATED",
                            msg,
                            "/admin/elections",
                            Severity.INFO,
                            LocalDateTime.now()
                    ));
                } catch (Exception e) {
                    log.error("Failed to notify admin {} of new election call", admin.getId(), e);
                }
            });
        } catch (Exception e) {
            log.error("Error processing ElectionCallCreatedEvent notification", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCandidacySubmitted(CandidacySubmittedEvent event) {
        try {
            CandidateApplication app = event.application();
            User applicant = app.getUser();
            String callTitre = app.getCall().getTitre();
            String position  = app.getPosition().getLabel();

            store.push(applicant.getId(), new NotificationDto(
                    UUID.randomUUID().toString(),
                    "CANDIDACY_SUBMITTED",
                    "Votre candidature pour " + position + " dans «" + callTitre + "» a bien été reçue.",
                    electionsPath(applicant),
                    Severity.SUCCESS,
                    LocalDateTime.now()
            ));

            String adminMsg = applicant.getPrenom() + " " + applicant.getNom()
                    + " a soumis sa candidature pour le poste de " + position + " dans «" + callTitre + "».";
            userRepository.findByRole(Role.ADMIN).forEach(admin -> {
                try {
                    store.push(admin.getId(), new NotificationDto(
                            UUID.randomUUID().toString(),
                            "CANDIDACY_SUBMITTED",
                            adminMsg,
                            "/admin/elections",
                            Severity.INFO,
                            LocalDateTime.now()
                    ));
                } catch (Exception e) {
                    log.error("Failed to notify admin {} of new candidacy", admin.getId(), e);
                }
            });
        } catch (Exception e) {
            log.error("Error processing CandidacySubmittedEvent notification", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationStatusChanged(ApplicationStatusChangedEvent event) {
        try {
            CandidateApplication app = event.application();
            User applicant = app.getUser();
            String callTitre = app.getCall().getTitre();
            String position  = app.getPosition().getLabel();
            boolean approved = event.newStatus() == ApprovalStatus.APPROVED;

            String msg = approved
                    ? "Félicitations ! Votre candidature pour " + position + " dans «" + callTitre + "» a été approuvée."
                    : "Votre candidature pour " + position + " dans «" + callTitre + "» n'a pas été retenue.";

            store.push(applicant.getId(), new NotificationDto(
                    UUID.randomUUID().toString(),
                    approved ? "CANDIDACY_APPROVED" : "CANDIDACY_REJECTED",
                    msg,
                    electionsPath(applicant),
                    approved ? Severity.SUCCESS : Severity.ERROR,
                    LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Error processing ApplicationStatusChangedEvent notification", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onElectionPublished(ElectionPublishedEvent event) {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String titre = event.election().getTitre();
            String start = event.election().getDateDebut().format(fmt);
            String end   = event.election().getDateFin().format(fmt);
            String msg   = "L'élection «" + titre + "» est ouverte au vote du " + start + " au " + end + ".";

            userRepository.findAll().stream()
                    .filter(User::isActif)
                    .forEach(user -> {
                        try {
                            store.push(user.getId(), new NotificationDto(
                                    UUID.randomUUID().toString(),
                                    "ELECTION_OPENED",
                                    msg,
                                    electionsPath(user),
                                    Severity.INFO,
                                    LocalDateTime.now()
                            ));
                        } catch (Exception e) {
                            log.error("Failed to notify user {} of election opening", user.getId(), e);
                        }
                    });
        } catch (Exception e) {
            log.error("Error processing ElectionPublishedEvent notification", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onElectionResultsPublished(ElectionResultsPublishedEvent event) {
        try {
            String titre = event.election().getTitre();
            String msg   = "Les résultats de l'élection «" + titre + "» sont disponibles.";

            userRepository.findAll().stream()
                    .filter(User::isActif)
                    .forEach(user -> {
                        try {
                            String link = switch (user.getRole()) {
                                case ADMIN         -> "/admin/elections";
                                case MEMBRE_BUREAU -> "/bureau/elections";
                                default            -> "/adherent/annonces";
                            };
                            Severity severity = user.getRole() == Role.ADMIN ? Severity.SUCCESS : Severity.INFO;
                            store.push(user.getId(), new NotificationDto(
                                    UUID.randomUUID().toString(),
                                    "ELECTION_RESULTS_PUBLISHED",
                                    msg,
                                    link,
                                    severity,
                                    LocalDateTime.now()
                            ));
                        } catch (Exception e) {
                            log.error("Failed to notify user {} of election results", user.getId(), e);
                        }
                    });
        } catch (Exception e) {
            log.error("Error processing ElectionResultsPublishedEvent notification", e);
        }
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSondageOpened(SondageOpenedEvent event) {
        try {
            String msg = "Nouveau sondage disponible : «" + event.sondage().getTitre() + "». Votez dès maintenant !";
            userRepository.findAll().stream()
                    .filter(User::isActif)
                    .forEach(user -> {
                        try {
                            store.push(user.getId(), new NotificationDto(
                                    UUID.randomUUID().toString(),
                                    "SONDAGE_OUVERT",
                                    msg,
                                    sondagePath(user),
                                    Severity.INFO,
                                    LocalDateTime.now()
                            ));
                        } catch (Exception e) {
                            log.error("Failed to notify user {} of new sondage", user.getId(), e);
                        }
                    });
        } catch (Exception e) {
            log.error("Error processing SondageOpenedEvent notification", e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSondageClosed(SondageClosedEvent event) {
        try {
            String msg = "Le sondage «" + event.sondage().getTitre() + "» est maintenant fermé. Les résultats sont disponibles.";
            userRepository.findAll().stream()
                    .filter(User::isActif)
                    .forEach(user -> {
                        try {
                            store.push(user.getId(), new NotificationDto(
                                    UUID.randomUUID().toString(),
                                    "SONDAGE_FERME",
                                    msg,
                                    sondagePath(user),
                                    Severity.INFO,
                                    LocalDateTime.now()
                            ));
                        } catch (Exception e) {
                            log.error("Failed to notify user {} of closed sondage", user.getId(), e);
                        }
                    });
        } catch (Exception e) {
            log.error("Error processing SondageClosedEvent notification", e);
        }
    }

    private String sondagePath(User user) {
        return switch (user.getRole()) {
            case ADMIN         -> "/admin/sondages";
            case MEMBRE_BUREAU -> "/bureau/sondages";
            default            -> "/adherent/sondages";
        };
    }

    private String electionsPath(User user) {
        return switch (user.getRole()) {
            case ADMIN         -> "/admin/elections";
            case MEMBRE_BUREAU -> "/bureau/elections";
            default            -> "/adherent/elections";
        };
    }
}
