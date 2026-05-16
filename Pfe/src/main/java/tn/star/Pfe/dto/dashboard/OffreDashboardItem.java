package tn.star.Pfe.dto.dashboard;

import tn.star.Pfe.enums.OfferStatus;

public record OffreDashboardItem(
        Long id,
        String titre,
        OfferStatus statut,
        int placesRestantes,
        long totalInscrits
) {}