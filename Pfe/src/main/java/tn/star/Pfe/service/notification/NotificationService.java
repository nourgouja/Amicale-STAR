package tn.star.Pfe.service.notification;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationStore store;
    private final UserRepository userRepository;
    private final IEmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAdhesionDemande(AdhesionDemandeEvent event) {
        var a = event.adherent();
        userRepository.findByRole(Role.ADMIN).forEach(admin ->
                store.push(admin.getId(), new NotificationDto(
                        UUID.randomUUID().toString(),
                        "ADHESION_DEMANDE",
                        "New adhesion request from " + a.getPrenom() + " " + a.getNom() + " (" + a.getMatriculeStar() + ")",
                        "/admin/adhesions",
                        NotificationDto.Severity.INFO,
                        LocalDateTime.now()
                ))
        );
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEcheanceOverdue(EcheanceOverdueEvent event) {
        var echeance = event.echeance();
        var adherent = echeance.getInscription().getAdherent();
        var offre = echeance.getInscription().getOffre();

        store.push(adherent.getId(), new NotificationDto(
                UUID.randomUUID().toString(),
                "ECHEANCE_OVERDUE",
                "OVERDUE: Payment of " + echeance.getMontant() + " DT for \"" + offre.getTitre() + "\" was due " + echeance.getDateEcheance(),
                "/home",
                Severity.ERROR,
                LocalDateTime.now()
        ));

        userRepository.findByRole(Role.MEMBRE_BUREAU).forEach(u -> {
            if (u instanceof MembreBureau) {
                store.push(u.getId(), new NotificationDto(
                        UUID.randomUUID().toString(),
                        "MEMBRE_ECHEANCE_OVERDUE",
                        adherent.getPrenom() + " " + adherent.getNom() + " has an overdue payment of " + echeance.getMontant() + " DT on \"" + offre.getTitre() + "\"",
                        "/home",
                        Severity.WARNING,
                        LocalDateTime.now()
                ));
            }
        });
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmailFailed(EmailFailedEvent event) {
        userRepository.findByRole(Role.ADMIN).forEach(admin ->
                store.push(admin.getId(), new NotificationDto(
                        UUID.randomUUID().toString(),
                        "EMAIL_FAILED",
                        "Email delivery failed for " + event.recipientEmail() + ": " + event.reason(),
                        null,
                        Severity.ERROR,
                        LocalDateTime.now()
                ))
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOffreCreated(OffreCreatedEvent event) {
        // Notify all adherents of new offer
        userRepository.findByRole(Role.ADHERENT).forEach(u ->
                store.push(u.getId(), new NotificationDto(
                        UUID.randomUUID().toString(),
                        "NOUVELLE_OFFRE",
                        "New offer available: \"" + event.offre().getTitre() + "\"",
                        "/home",
                        Severity.INFO,
                        LocalDateTime.now()
                ))
        );
    }
}
