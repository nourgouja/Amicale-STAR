package tn.star.Pfe.controller.echeance;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.star.Pfe.dto.paiement.EcheanceResponse;
import tn.star.Pfe.service.echeance.IEcheanceService;

import java.util.List;

@RestController
@RequestMapping("/api/echeances")
@RequiredArgsConstructor
public class EcheanceController {

    private final IEcheanceService echeanceService;

    @GetMapping("/inscription/{inscriptionId}")
    @PreAuthorize("hasRole('MEMBRE_BUREAU') or hasRole('ADMIN')")
    public ResponseEntity<List<EcheanceResponse>> parInscription(
            @PathVariable Long inscriptionId) {
        return ResponseEntity.ok(echeanceService.parInscription(inscriptionId));
    }

    @PatchMapping("/{id}/payer")
    @PreAuthorize("hasRole('ADMIN') or @echeanceAuthService.canValidate(principal, #id)")
    public ResponseEntity<EcheanceResponse> marquerPayee(@PathVariable Long id) {
        return ResponseEntity.ok(echeanceService.marquerPayee(id));
    }
}
