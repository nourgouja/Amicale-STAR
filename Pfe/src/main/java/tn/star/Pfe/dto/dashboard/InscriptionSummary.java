package tn.star.Pfe.dto.dashboard;

import tn.star.Pfe.enums.ApprovalStatus;
import tn.star.Pfe.enums.TypeOffre;

import java.time.LocalDate;

public record InscriptionSummary(
        Long   inscriptionId,
        String offreTitre,
        TypeOffre offreType,
        LocalDate dateDebut,
        ApprovalStatus statut
) {}
