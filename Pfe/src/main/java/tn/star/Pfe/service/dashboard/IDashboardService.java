package tn.star.Pfe.service.dashboard;

import tn.star.Pfe.dto.dashboard.AdherentDashboardResponse;
import tn.star.Pfe.dto.dashboard.AdminDashboardResponse;
import tn.star.Pfe.dto.dashboard.BureauDashboardResponse;

public interface IDashboardService {
    AdminDashboardResponse    getAdminDashboard();
    BureauDashboardResponse   getBureauDashboard(String email);
    AdherentDashboardResponse getAdherentDashboard(Long adherentId);
}
