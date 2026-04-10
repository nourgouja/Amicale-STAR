package tn.star.Pfe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.star.Pfe.dto.offre.OffreRequest;
import tn.star.Pfe.dto.offre.OffreResponse;
import tn.star.Pfe.enums.TypeOffre;
import tn.star.Pfe.service.OffreService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/offres")
public class OffreController {

    private final OffreService offreService;


    @GetMapping
    public ResponseEntity<List<OffreResponse>> listerOuvertes() {
        return ResponseEntity.ok(offreService.listerOffresOuvertes());
    }


    @GetMapping("/{id}")
    public ResponseEntity<OffreResponse> trouverParId(
            @PathVariable Long id) {
        return ResponseEntity.ok(offreService.trouverParId(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<OffreResponse>> rechercher(
            @RequestParam String titre) {
        return ResponseEntity.ok(offreService.rechercherParTitre(titre));
    }

    @PostMapping(value = "/creer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MEMBRE_BUREAU') or hasRole('ADMIN')")
    public ResponseEntity<OffreResponse> creer(
            @RequestPart("offre") @Valid OffreRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        return ResponseEntity.status(201).body(offreService.creer(request, image));
    }

    @PutMapping("/modifier/{id}")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU','ADMIN')")
    public ResponseEntity<OffreResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody OffreRequest.UpdateOffreRequest req) {
        return ResponseEntity.ok(offreService.modifier(id, req));
    }

    @PatchMapping("/fermer/{id}")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU','ADMIN')")
    public ResponseEntity<OffreResponse> fermer(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.fermer(id));
    }

    @DeleteMapping("/supprimer/{id}")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU','ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        offreService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/publier/{id}")
    @PreAuthorize("hasRole('MEMBRE_BUREAU') or hasRole('ADMIN')")
    public ResponseEntity<OffreResponse> publier(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.publier(id));
    }

    @PatchMapping("/archiver/{id}")
    @PreAuthorize("hasRole('MEMBRE_BUREAU') or hasRole('ADMIN')")
    public ResponseEntity<OffreResponse> archiver(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.archiver(id));
    }
}