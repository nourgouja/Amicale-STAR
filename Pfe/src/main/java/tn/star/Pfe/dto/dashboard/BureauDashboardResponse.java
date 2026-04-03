package tn.star.Pfe.dto.dashboard;

import tn.star.Pfe.dto.inscription.InscriptionResponse;
import java.util.List;

public record BureauDashboardResponse(

        List<OffreDashboardItem> mesOffres,

        long totalInscriptionsEnAttente,
        List<InscriptionResponse> inscriptionsEnAttente,

        long totalPaiementsEnAttente,

        List<InscriptionResponse> paiementsEnAttente,

        List<ParticipationItem> participationParOffre
) {}