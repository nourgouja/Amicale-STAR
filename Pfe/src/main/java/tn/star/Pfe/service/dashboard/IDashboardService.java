package tn.star.Pfe.service.dashboard;

import tn.star.Pfe.dto.dashboard.AdminDashboardResponse;
import tn.star.Pfe.dto.dashboard.BureauDashboardResponse;
import tn.star.Pfe.dto.dashboard.TresorierDashboardResponse;

public interface IDashboardService {
    AdminDashboardResponse getAdminDashboard();
    BureauDashboardResponse getBureauDashboard();
    TresorierDashboardResponse getTresorierDashboard();
}
