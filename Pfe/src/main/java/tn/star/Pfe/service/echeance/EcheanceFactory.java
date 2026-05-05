package tn.star.Pfe.service.echeance;

import tn.star.Pfe.entity.inscription.Echeance;
import tn.star.Pfe.entity.inscription.Inscription;
import tn.star.Pfe.enums.PeriodePaiement;
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
            PeriodePaiement periode) {

        int nbTranches = switch (periode) {
            case COMPTANT    -> 1;
            case SEMESTRIEL  -> 2;
            case TRIMESTRIEL -> 4;
            case MENSUEL     -> 12;
        };

        int daysBetween = switch (periode) {
            case COMPTANT    -> 0;
            case SEMESTRIEL  -> 180;
            case TRIMESTRIEL -> 90;
            case MENSUEL     -> 30;
        };

        BigDecimal montantParTranche = prixTotal
                .divide(BigDecimal.valueOf(nbTranches), 2, RoundingMode.HALF_UP);
        // last tranche absorbs rounding so the sum always equals prixTotal
        BigDecimal derniereTranche = prixTotal.subtract(
                montantParTranche.multiply(BigDecimal.valueOf(nbTranches - 1)));

        List<Echeance> result = new ArrayList<>();
        LocalDate base = LocalDate.now().plusDays(7);

        for (int i = 0; i < nbTranches; i++) {
            BigDecimal montant = (i == nbTranches - 1) ? derniereTranche : montantParTranche;
            result.add(Echeance.builder()
                    .inscription(inscription)
                    .numero(i + 1)
                    .montant(montant)
                    .dateEcheance(base.plusDays((long) daysBetween * i))
                    .statut(StatutPaiement.EN_ATTENTE)
                    .build());
        }

        return result;
    }
}
