package tn.star.Pfe.controller.sondage;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.star.Pfe.dto.sondage.SondageRequest;
import tn.star.Pfe.dto.sondage.SondageResponse;
import tn.star.Pfe.dto.sondage.UpdateSondageRequest;
import tn.star.Pfe.dto.sondage.VoteRequest;
import tn.star.Pfe.service.sondage.ISondageService;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sondages")
public class SondageController {

    private final ISondageService sondageService;

    @GetMapping
    public ResponseEntity<List<SondageResponse>> lister(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(sondageService.lister(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SondageResponse> trouverParId(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(sondageService.trouverParId(id, userDetails.getUsername()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADHERENT')")
    public ResponseEntity<SondageResponse> creer(
            @RequestPart("req") @Valid SondageRequest req,
            @RequestPart(value = "image1", required = false) MultipartFile image1,
            @RequestPart(value = "image2", required = false) MultipartFile image2,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sondageService.creer(req, image1, image2, userDetails.getUsername()));
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADHERENT')")
    public ResponseEntity<SondageResponse> modifier(
            @PathVariable Long id,
            @RequestPart("req") @Valid UpdateSondageRequest req,
            @RequestPart(value = "image1", required = false) MultipartFile image1,
            @RequestPart(value = "image2", required = false) MultipartFile image2,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        return ResponseEntity.ok(sondageService.modifier(id, req, image1, image2, userDetails.getUsername()));
    }

    @PatchMapping("/{id}/activer")
    @PreAuthorize("hasRole('ADHERENT')")
    public ResponseEntity<SondageResponse> activer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(sondageService.activer(id, userDetails.getUsername()));
    }

    @PatchMapping("/{id}/fermer")
    public ResponseEntity<SondageResponse> fermer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(sondageService.fermer(id, userDetails.getUsername()));
    }

    @PatchMapping("/{id}/archiver")
    @PreAuthorize("hasRole('ADHERENT')")
    public ResponseEntity<SondageResponse> archiver(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(sondageService.archiver(id, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADHERENT')")
    public ResponseEntity<Void> supprimer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        sondageService.supprimer(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/voter")
    @PreAuthorize("hasRole('ADHERENT')")
    public ResponseEntity<SondageResponse> voter(
            @PathVariable Long id,
            @Valid @RequestBody VoteRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(sondageService.voter(id, req.getOptionId(), userDetails.getUsername()));
    }

    @GetMapping("/{id}/resultats")
    public ResponseEntity<SondageResponse> resultats(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(sondageService.trouverParId(id, userDetails.getUsername()));
    }
}
