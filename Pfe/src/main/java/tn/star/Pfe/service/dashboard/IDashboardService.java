package tn.star.Pfe.service.dashboard;

import tn.star.Pfe.dto.dashboard.AdherentDashboardResponse;
import tn.star.Pfe.dto.dashboard.AdminDashboardResponse;
import tn.star.Pfe.dto.dashboard.BureauDashboardResponse;
import tn.star.Pfe.dto.dashboard.TresorierDashboardResponse;

public interface IDashboardService {
    AdminDashboardResponse    getAdminDashboard();
    BureauDashboardResponse   getBureauDashboard(String email);   // now takes email for scope
    TresorierDashboardResponse getTresorierDashboard();
    AdherentDashboardResponse getAdherentDashboard(Long adherentId); // new
}
