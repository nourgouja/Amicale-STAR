package tn.star.Pfe.dto.dashboard;

import tn.star.Pfe.dto.offre.OffreResponse;

import java.math.BigDecimal;
import java.util.List;

public record AdherentDashboardResponse(
        long totalInscriptions,
        long inscriptionsConfirmees,
        long inscriptionsEnAttente,
        long inscriptionsAnnulees,
        long echeancesEnAttente,
        long echeancesEnRetard,
        BigDecimal montantDuTotal,
        List<OffreResponse> offresDisponibles,
        List<InscriptionSummary> prochainsEvenements,
        List<EcheanceSummary> prochainesEcheances
) {}
