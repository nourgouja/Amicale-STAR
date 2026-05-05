package tn.star.Pfe.controller.sondage;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.star.Pfe.dto.sondage.SondageRequest;
import tn.star.Pfe.dto.sondage.SondageResponse;
import tn.star.Pfe.dto.sondage.UpdateSondageRequest;
import tn.star.Pfe.dto.sondage.VoteRequest;
import tn.star.Pfe.security.UserPrincipal;
import tn.star.Pfe.service.sondage.ISondageService;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sondages")
//@SecurityRequirement(name = "bearerAuth")
public class SondageController {

    private final ObjectMapper objectMapper;
    private final ISondageService sondageService;
    private static final long MAX_IMAGE_BYTES = 1_000_000L;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SondageResponse>> lister(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sondageService.lister(principal.getUsername()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SondageResponse> trouverParId(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sondageService.trouverParId(id, principal.getUsername()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBRE_BUREAU')")
    public ResponseEntity<SondageResponse> creer(
            @RequestPart("sondage") String req,
            @RequestPart(value = "image1", required = false) MultipartFile image1,
            @RequestPart(value = "image2", required = false) MultipartFile image2,
            @AuthenticationPrincipal UserPrincipal principal
    ) throws IOException {

        SondageRequest request =
                objectMapper.readValue(req, SondageRequest.class);

        validateImages(image1, image2);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sondageService.creer(
                        request,
                        image1,
                        image2,
                        principal.getUsername()
                ));
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBRE_BUREAU')")
    public ResponseEntity<SondageResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestPart("sondage") UpdateSondageRequest req,
            @RequestPart(value = "image1", required = false) MultipartFile image1,
            @RequestPart(value = "image2", required = false) MultipartFile image2,
            @AuthenticationPrincipal UserPrincipal principal) throws IOException {
        validateImages(image1, image2);
        return ResponseEntity.ok(sondageService.modifier(id, req, image1, image2, principal.getUsername()));
    }

    @PatchMapping("/{id}/activer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBRE_BUREAU')")
    public ResponseEntity<SondageResponse> activer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sondageService.activer(id, principal.getUsername()));
    }

    @PatchMapping("/{id}/fermer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBRE_BUREAU')")
    public ResponseEntity<SondageResponse> fermer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sondageService.fermer(id, principal.getUsername()));
    }

    @PatchMapping("/{id}/archiver")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBRE_BUREAU')")
    public ResponseEntity<SondageResponse> archiver(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sondageService.archiver(id, principal.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBRE_BUREAU')")
    public ResponseEntity<Void> supprimer(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        sondageService.supprimer(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/voter")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SondageResponse> voter(
            @PathVariable Long id,
            @Valid @RequestBody VoteRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sondageService.voter(id, req.getOptionId(), principal.getUsername()));
    }

    private void validateImages(MultipartFile image1, MultipartFile image2) {
        if (image1 != null && !image1.isEmpty() && image1.getSize() > MAX_IMAGE_BYTES) {
            throw new tn.star.Pfe.exceptions.BadRequestException("Image 1 ne peut pas dépasser 1 Mo");
        }
        if (image2 != null && !image2.isEmpty() && image2.getSize() > MAX_IMAGE_BYTES) {
            throw new tn.star.Pfe.exceptions.BadRequestException("Image 2 ne peut pas dépasser 1 Mo");
        }
    }
}
