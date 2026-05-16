package tn.star.Pfe.event;

import tn.star.Pfe.entity.inscription.Inscription;
import tn.star.Pfe.enums.ApprovalStatus;

public record InscriptionStatusChangedEvent (
        Inscription inscription,
        ApprovalStatus oldStatut,
        ApprovalStatus newStatut
) {
}
