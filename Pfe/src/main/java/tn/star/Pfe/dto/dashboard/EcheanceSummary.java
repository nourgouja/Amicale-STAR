package tn.star.Pfe.dto.dashboard;

import tn.star.Pfe.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EcheanceSummary(
        Long echeanceId,
        String offreTitre,
        BigDecimal montant,
        LocalDate dateEcheance,
        PaymentStatus statut,
        Integer numero
) {}
