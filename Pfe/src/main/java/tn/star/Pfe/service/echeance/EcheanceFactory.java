package tn.star.Pfe.service.echeance;

import tn.star.Pfe.entity.Echeance;
import tn.star.Pfe.entity.Inscription;
import tn.star.Pfe.enums.ModePaiement;
import tn.star.Pfe.enums.StatutPaiement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EcheanceFactory {

    public static List<Echeance> generate(
            Inscription inscription,
            BigDecimal prixTotal,
            ModePaiement mode) {

        int nbEcheances = switch (mode) {
            case FULL       -> 1;
            case TIERS      -> 3;
            case SEMESTRIEL -> 6;
        };

        BigDecimal montantParEcheance = prixTotal
                .divide(BigDecimal.valueOf(nbEcheances), 2, RoundingMode.HALF_UP);

        List<Echeance> result = new ArrayList<>();
        LocalDate base = LocalDate.now();

        for (int i = 0; i < nbEcheances; i++) {
            result.add(Echeance.builder()
                    .inscription(inscription)
                    .numero(i + 1)
                    .montant(montantParEcheance)
                    .dateEcheance(base.plusMonths(i))
                    .statut(StatutPaiement.EN_ATTENTE)
                    .build());
        }

        return result;
    }
}
