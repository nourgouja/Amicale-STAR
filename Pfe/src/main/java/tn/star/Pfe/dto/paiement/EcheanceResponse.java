package tn.star.Pfe.dto.paiement;

import tn.star.Pfe.enums.StatutPaiement;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EcheanceResponse(
        Long id,
        Long inscriptionId,
        Integer numero,
        BigDecimal montant,
        LocalDate dateEcheance,
        StatutPaiement statut
) {}
