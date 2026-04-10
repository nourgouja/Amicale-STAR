package tn.star.Pfe.dto.dashboard;

import java.math.BigDecimal;
import java.util.Map;

public record TresorierDashboardResponse(
        Map<String, BigDecimal> collecteParPole,
        Map<String, BigDecimal> attenduParPole,
        long totalEnRetard
) {}