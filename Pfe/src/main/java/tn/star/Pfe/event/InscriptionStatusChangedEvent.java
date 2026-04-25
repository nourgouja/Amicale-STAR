package tn.star.Pfe.event;

import tn.star.Pfe.entity.Inscription;
import tn.star.Pfe.enums.StatutInscription;

public record InscriptionStatusChangedEvent (
        Inscription inscription,
        StatutInscription oldStatut,
        StatutInscription newStatut
) {
}
