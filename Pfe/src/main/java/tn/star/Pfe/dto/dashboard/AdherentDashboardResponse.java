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
        List<OffreResponse> offresDisponibles,       // up to 6 open offers not yet joined
        List<InscriptionSummary> prochainsEvenements, // next 3 upcoming confirmed events
        List<EcheanceSummary> prochainesEcheances     // next 3 unpaid deadlines
) {}
