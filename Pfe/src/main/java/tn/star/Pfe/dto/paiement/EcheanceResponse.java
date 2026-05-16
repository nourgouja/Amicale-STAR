package tn.star.Pfe.dto.paiement;

import tn.star.Pfe.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record EcheanceResponse(
        Long id,
        Long inscriptionId,
        Integer numero,
        BigDecimal montant,
        LocalDate dateEcheance,
        PaymentStatus statut,
        LocalDateTime datePaiement,
        int daysUntilDue,
        boolean overdue,
        String adherentNom,
        String adherentPrenom,
        String offreTitre,
        String adherentEmail,
        String periodePaiement,
        String inscriptionStatut
) {}
