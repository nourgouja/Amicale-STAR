package tn.star.Pfe.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.star.Pfe.dto.dashboard.AdminDashboardResponse;
import tn.star.Pfe.dto.dashboard.BureauDashboardResponse;
import tn.star.Pfe.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardResponse> adminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping
    @PreAuthorize("hasRole('MEMBRE_BUREAU')")
    public ResponseEntity<BureauDashboardResponse> bureauDashboard() {
        return ResponseEntity.ok(dashboardService.getBureauDashboard());
    }
}