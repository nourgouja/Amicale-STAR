package tn.star.Pfe.dto.dashboard;

import java.util.Map;

public record AdminDashboardResponse(

        long totalUtilisateurs,
        Map<String, Long> parRole,

        Map<String, Long> offres,

        Map<String, Long> inscriptions,

        Map<String, Long> paiements

) {}