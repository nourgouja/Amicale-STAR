package tn.star.Pfe.service.echeance;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tn.star.Pfe.dto.notification.NotificationDto;
import tn.star.Pfe.dto.paiement.EcheanceResponse;
import tn.star.Pfe.entity.inscription.Echeance;
import tn.star.Pfe.entity.inscription.Inscription;
import tn.star.Pfe.enums.StatutPaiement;
import tn.star.Pfe.event.EcheanceOverdueEvent;
import tn.star.Pfe.exceptions.BadRequestException;
import tn.star.Pfe.exceptions.NotFoundException;
import tn.star.Pfe.repository.EcheanceRepository;
import tn.star.Pfe.repository.InscriptionRepository;
import tn.star.Pfe.service.email.IEmailService;
import tn.star.Pfe.service.notification.NotificationStore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EcheanceService implements IEcheanceService {

    private final EcheanceRepository echeanceRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ApplicationEventPublisher publisher;
    private final NotificationStore notificationStore;
    private final IEmailService emailService;

    public List<EcheanceResponse> parInscription(Long inscriptionId) {
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new NotFoundException("Inscription introuvable"));

        return echeanceRepository.findByInscription(inscription)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EcheanceResponse> nonPayees() {
        return echeanceRepository
                .findByStatutIn(List.of(StatutPaiement.EN_ATTENTE, StatutPaiement.EN_RETARD))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EcheanceResponse> toutes() {
        return echeanceRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EcheanceResponse> prochainesEcheances() {
        LocalDate today   = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);
        return echeanceRepository
                .findByDateEcheanceBetweenAndStatut(today, in7Days, StatutPaiement.EN_ATTENTE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EcheanceResponse marquerPayee(Long echeanceId) {
        Echeance echeance = echeanceRepository.findById(echeanceId)
                .orElseThrow(() -> new NotFoundException("Échéance introuvable"));

        if (echeance.getStatut() == StatutPaiement.PAYEE)
            throw new BadRequestException("Cette échéance est déjà payée.");

        echeance.setStatut(StatutPaiement.PAYEE);
        echeance.setDatePaiement(LocalDateTime.now());
        Echeance saved = echeanceRepository.save(echeance);

        notifyPaiementRecu(saved);
        return toResponse(saved);
    }

    @Transactional
    public void marquerEnRetard() {
        List<Echeance> enRetard = echeanceRepository
                .findByStatutAndDateEcheanceBefore(StatutPaiement.EN_ATTENTE, LocalDate.now());

        enRetard.forEach(e -> {
            e.setStatut(StatutPaiement.EN_RETARD);
            publisher.publishEvent(new EcheanceOverdueEvent(e));
        });
        echeanceRepository.saveAll(enRetard);

        if (!enRetard.isEmpty())
            log.info("{} échéance(s) marquées EN_RETARD", enRetard.size());
    }

    public void envoyerRappels() {
        List<Echeance> unpaid = echeanceRepository
                .findByStatutIn(List.of(StatutPaiement.EN_ATTENTE, StatutPaiement.EN_RETARD));

        for (Echeance e : unpaid) {
            int days = e.getDaysUntilDue();
            if      (days ==  7) sendRappel(e, RappelType.J_MOINS_7);
            else if (days ==  3) sendRappel(e, RappelType.J_MOINS_3);
            else if (days ==  0) sendRappel(e, RappelType.JOUR_J);
            else if (days == -7) sendRappel(e, RappelType.J_PLUS_7);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void notifyPaiementRecu(Echeance e) {
        Inscription ins  = e.getInscription();
        long total = ins.getEcheances().size();
        long paid  = ins.getEcheances().stream()
                .filter(ec -> ec.getStatut() == StatutPaiement.PAYEE).count();

        String msg = paid == total
                ? "Tous les versements pour \"" + ins.getOffre().getTitre() + "\" ont été reçus."
                : "Versement " + e.getNumero() + "/" + total + " reçu pour \""
                  + ins.getOffre().getTitre() + "\" (" + e.getMontant() + " DT).";

        notificationStore.push(ins.getAdherent().getId(), new NotificationDto(
                UUID.randomUUID().toString(),
                "PAIEMENT_RECU",
                msg,
                "/adherent/inscriptions",
                NotificationDto.Severity.SUCCESS,
                LocalDateTime.now()
        ));
    }

    private void sendRappel(Echeance e, RappelType type) {
        try {
            var adherent = e.getInscription().getAdherent();
            var offre    = e.getInscription().getOffre();
            int total    = e.getInscription().getEcheances().size();

            emailService.sendEcheanceReminder(
                    adherent.getEmail(),
                    adherent.getPrenom(),
                    offre.getTitre(),
                    e.getNumero(),
                    total,
                    e.getMontant(),
                    e.getDateEcheance(),
                    type.name()
            );

            notificationStore.push(adherent.getId(), new NotificationDto(
                    UUID.randomUUID().toString(),
                    "RAPPEL_ECHEANCE",
                    type.notifMessage(e),
                    "/adherent/inscriptions",
                    type.severity(),
                    LocalDateTime.now()
            ));

            log.info("Rappel {} envoyé pour échéance {} (adherent={})", type, e.getId(), adherent.getId());
        } catch (Exception ex) {
            log.error("Échec rappel {} pour échéance {}: {}", type, e.getId(), ex.getMessage());
        }
    }

    EcheanceResponse toResponse(Echeance e) {
        var ins = e.getInscription();
        var adherent = ins.getAdherent();
        return new EcheanceResponse(
                e.getId(),
                ins.getId(),
                e.getNumero(),
                e.getMontant(),
                e.getDateEcheance(),
                e.getStatut(),
                e.getDatePaiement(),
                e.getDaysUntilDue(),
                e.isOverdue(),
                adherent.getNom(),
                adherent.getPrenom(),
                ins.getOffre().getTitre(),
                adherent.getEmail(),
                ins.getPeriodePaiement() != null ? ins.getPeriodePaiement().name() : null,
                ins.getStatut() != null ? ins.getStatut().name() : null
        );
    }

    // ── Inner enum ────────────────────────────────────────────────────────────

    enum RappelType {
        J_MOINS_7 {
            @Override public String notifMessage(Echeance e) {
                return "Rappel : versement de " + e.getMontant() + " DT dans 7 jours.";
            }
            @Override public NotificationDto.Severity severity() { return NotificationDto.Severity.INFO; }
        },
        J_MOINS_3 {
            @Override public String notifMessage(Echeance e) {
                return "Rappel : versement de " + e.getMontant() + " DT dans 3 jours.";
            }
            @Override public NotificationDto.Severity severity() { return NotificationDto.Severity.WARNING; }
        },
        JOUR_J {
            @Override public String notifMessage(Echeance e) {
                return "Versement de " + e.getMontant() + " DT à régler aujourd'hui.";
            }
            @Override public NotificationDto.Severity severity() { return NotificationDto.Severity.WARNING; }
        },
        J_PLUS_7 {
            @Override public String notifMessage(Echeance e) {
                return "Versement de " + e.getMontant() + " DT en retard de 7 jours — réglez dès que possible.";
            }
            @Override public NotificationDto.Severity severity() { return NotificationDto.Severity.ERROR; }
        };

        public abstract String notifMessage(Echeance e);
        public abstract NotificationDto.Severity severity();
    }
}
