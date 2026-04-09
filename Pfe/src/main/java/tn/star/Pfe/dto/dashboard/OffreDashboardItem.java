package tn.star.Pfe.dto.dashboard;

import tn.star.Pfe.enums.StatutOffre;

public record OffreDashboardItem(
        Long id,
        String titre,
        StatutOffre statut,
        int placesRestantes,
        long totalInscrits
) {}