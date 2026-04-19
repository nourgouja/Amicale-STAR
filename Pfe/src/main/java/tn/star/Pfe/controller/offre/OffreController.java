package tn.star.Pfe.controller.offre;

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
import tn.star.Pfe.dto.offre.OffreRequest;
import tn.star.Pfe.dto.offre.OffreResponse;
import tn.star.Pfe.service.offre.IOffreService;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/offres")
public class OffreController {

    private final IOffreService offreService;

    @GetMapping
    public ResponseEntity<List<OffreResponse>> listerOuvertes() {
        return ResponseEntity.ok(offreService.listerOffresOuvertes());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<List<OffreResponse>> listerToutes() {
        return ResponseEntity.ok(offreService.listerToutes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OffreResponse> trouverParId(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.trouverParId(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<OffreResponse>> rechercher(@RequestParam String titre) {
        return ResponseEntity.ok(offreService.rechercherParTitre(titre));
    }

    @PostMapping(value = "/creer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('MEMBRE_BUREAU') and " +
            "@offreAuthService.canCreate(principal, #req.typeOffre.name()))")
    public ResponseEntity<OffreResponse> creer(
            @RequestPart("req") OffreRequest req,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(offreService.creer(req, image, userDetails.getUsername()));
    }

    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MEMBRE_BUREAU') and @offreAuthService.canManage(principal, #id))")
    public ResponseEntity<OffreResponse> uploadImage(
            @PathVariable Long id,
            @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(offreService.uploadImage(id, image));
    }

    @PutMapping("/modifier/{id}")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<OffreResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody OffreRequest.UpdateOffreRequest req) {
        return ResponseEntity.ok(offreService.modifier(id, req));
    }

    @PatchMapping("/publier/{id}")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<OffreResponse> publier(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.publier(id));
    }

    @PatchMapping("/fermer/{id}")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<OffreResponse> fermer(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.fermer(id));
    }

    @PatchMapping("/archiver/{id}")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<OffreResponse> archiver(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.archiver(id));
    }

    @DeleteMapping("/supprimer/{id}")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        offreService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
