package tn.star.Pfe.controller.adhesion;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.star.Pfe.dto.auth.DemandeAdhesionResponse;
import tn.star.Pfe.dto.auth.DemandeRequest;
import tn.star.Pfe.service.adhesion.IAdhesionService;

import java.util.List;

@RestController
@RequestMapping("/api/adhesion")
@RequiredArgsConstructor
public class AdhesionController {

    private final IAdhesionService adhesionService;

    @PostMapping("/demande")
    public ResponseEntity<Void> soumettreDemande(@Valid @RequestBody DemandeRequest request) {
        adhesionService.soumettreDemande(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DemandeAdhesionResponse>> getDemandesEnAttente() {
        return ResponseEntity.ok(adhesionService.getDemandesEnAttente());
    }

    @PostMapping("/{id}/approuver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approuver(@PathVariable Long id) {
        adhesionService.approuver(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/rejeter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejeter(@PathVariable Long id) {
        adhesionService.rejeter(id);
        return ResponseEntity.noContent().build();
    }
}
