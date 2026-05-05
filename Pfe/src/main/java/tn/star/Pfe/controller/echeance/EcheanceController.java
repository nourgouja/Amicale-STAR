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
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN', 'ADHERENT')")
    public ResponseEntity<List<EcheanceResponse>> parInscription(@PathVariable Long inscriptionId) {
        return ResponseEntity.ok(echeanceService.parInscription(inscriptionId));
    }

    @GetMapping("/non-payees")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<List<EcheanceResponse>> nonPayees() {
        return ResponseEntity.ok(echeanceService.nonPayees());
    }

    @GetMapping("/toutes")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<List<EcheanceResponse>> toutes() {
        return ResponseEntity.ok(echeanceService.toutes());
    }

    @GetMapping("/prochaines")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<List<EcheanceResponse>> prochaines() {
        return ResponseEntity.ok(echeanceService.prochainesEcheances());
    }

    @PatchMapping("/{id}/payer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBRE_BUREAU')")
    public ResponseEntity<EcheanceResponse> marquerPayee(@PathVariable Long id) {
        return ResponseEntity.ok(echeanceService.marquerPayee(id));
    }
}
