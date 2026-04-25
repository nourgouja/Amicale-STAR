package tn.star.Pfe.controller.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.star.Pfe.dto.dashboard.AdherentDashboardResponse;
import tn.star.Pfe.dto.dashboard.AdminDashboardResponse;
import tn.star.Pfe.dto.dashboard.BureauDashboardResponse;
import tn.star.Pfe.dto.dashboard.TresorierDashboardResponse;
import tn.star.Pfe.security.UserPrincipal;
import tn.star.Pfe.service.dashboard.IDashboardService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> adminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/bureau/dashboard")
    @PreAuthorize("hasRole('MEMBRE_BUREAU')")
    public ResponseEntity<BureauDashboardResponse> bureauDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.getBureauDashboard(principal.getUsername()));
    }

    @GetMapping("/bureau/tresorier")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<TresorierDashboardResponse> tresorierDashboard() {
        return ResponseEntity.ok(dashboardService.getTresorierDashboard());
    }

    @GetMapping("/adherent/dashboard")
    @PreAuthorize("hasRole('ADHERENT')")
    public ResponseEntity<AdherentDashboardResponse> adherentDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(dashboardService.getAdherentDashboard(principal.getId()));
    }
}
