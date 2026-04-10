package tn.star.Pfe.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AdminDashboardResponse(
        long totalUtilisateurs,
        Map<String, Long> parRole,
        List<OffreDashboardItem> offres,
        long totalInscriptions,
        long enAttente,
        long confirmees,
        long annulees,
        long echeancesEnAttente,
        long echeancesEnRetard,
        long echeancesPayees,
        BigDecimal totalCollecte,
        BigDecimal totalAttendu
) {}