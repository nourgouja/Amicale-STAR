package tn.star.Pfe.dto.dashboard;

import tn.star.Pfe.enums.StatutPaiement;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EcheanceSummary(
        Long echeanceId,
        String offreTitre,
        BigDecimal montant,
        LocalDate dateEcheance,
        StatutPaiement statut,
        Integer numero
) {}
