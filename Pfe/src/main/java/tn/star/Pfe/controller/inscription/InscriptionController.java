//package tn.star.Pfe.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//import tn.star.Pfe.dto.inscription.InscriptionResponse;
//import tn.star.Pfe.entity.Adherent;
//import tn.star.Pfe.enums.StatutPaiement;
//import tn.star.Pfe.exceptions.EligibiliteException;
//import tn.star.Pfe.exceptions.NotFoundException;
//import tn.star.Pfe.repository.UserRepository;
//import tn.star.Pfe.security.UserPrincipal;
//import tn.star.Pfe.service.inscription.IInscriptionService;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/inscriptions")
//@RequiredArgsConstructor
//public class InscriptionController {
//
//    private final IInscriptionService inscriptionService;
//    private final UserRepository userRepository;
//
//    @PostMapping("/inscrire/{offreId}")
//    @PreAuthorize("hasRole('ADHERENT")
//    public ResponseEntity<InscriptionResponse> inscrire(
//            @PathVariable int offreId,
//            @AuthenticationPrincipal UserPrincipal principal) {
//
//        Object user = userRepository
//                .findByEmail(principal.getUsername())
//                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
//
//        if (!(user instanceof Adherent adherent))
//            throw new EligibiliteException("Seuls les adhérents peuvent s'inscrire");
//
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(inscriptionService.inscrire(offreId, adherent));
//    }
//
//    @PatchMapping("/annuler/{inscriptionId}")
//    public ResponseEntity<InscriptionResponse> annuler(
//            @PathVariable int inscriptionId,
//            @AuthenticationPrincipal UserPrincipal principal) {
//
//        Object user = userRepository
//                .findByEmail(principal.getUsername())
//                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
//
//        if (!(user instanceof Adherent adherent))
//            throw new EligibiliteException("Seuls les adhérents peuvent annuler");
//
//        return ResponseEntity.ok(inscriptionService.annuler(adherent, inscriptionId));
//    }
//
//    @GetMapping("/mesinscriptions")
//    public ResponseEntity<List<InscriptionResponse>> mesInscriptions(
//            @AuthenticationPrincipal UserPrincipal principal) {
//
//        Object user = userRepository
//                .findByEmail(principal.getUsername())
//                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
//
//        if (!(user instanceof Adherent adherent))
//            throw new EligibiliteException("Seuls les adhérents ont des inscriptions");
//
//        return ResponseEntity.ok(inscriptionService.mesInscriptions(adherent));
//    }
//
//    @PatchMapping("/confirmer/{inscriptionId}")
//    public ResponseEntity<InscriptionResponse> confirmer(@PathVariable int inscriptionId) {
//        return ResponseEntity.ok(inscriptionService.confirmer(inscriptionId));
//    }
//
//
//    @GetMapping("/offre/{offreId}")
//    public ResponseEntity<List<InscriptionResponse>> inscritsParOffre(@PathVariable int offreId) {
//        return ResponseEntity.ok(inscriptionService.inscritsParOffre(offreId));
//    }
//
//    @PutMapping("/{id}/paiement")
//    public ResponseEntity<InscriptionResponse> mettreAjourPaiement(
//            @PathVariable int id,
//            @RequestParam StatutPaiement statut) {
//        return ResponseEntity.ok(inscriptionService.mettreAjourPaiement(id, statut));
//    }
//
//}

package tn.star.Pfe.controller.inscription;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.star.Pfe.dto.inscription.InscriptionResponse;
import tn.star.Pfe.entity.Adherent;
import tn.star.Pfe.enums.StatutPaiement;
import tn.star.Pfe.exceptions.EligibiliteException;
import tn.star.Pfe.exceptions.NotFoundException;
import tn.star.Pfe.repository.UserRepository;
import tn.star.Pfe.security.UserPrincipal;
import tn.star.Pfe.service.inscription.IInscriptionService;

import java.util.List;

@RestController
@RequestMapping("/api/inscriptions")
@RequiredArgsConstructor
public class InscriptionController {

    private final IInscriptionService inscriptionService;
    private final UserRepository userRepository;

    @PostMapping("/inscrire/{offreId}")
    @PreAuthorize("hasRole('ADHERENT')")
    public ResponseEntity<InscriptionResponse> inscrire(
            @PathVariable @Parameter(description = "ID de l'offre", required = true, example = "1") Long offreId,
            @AuthenticationPrincipal @Parameter(hidden = true) UserPrincipal principal) {

        Adherent adherent = getAdherentFromPrincipal(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inscriptionService.inscrire(offreId, adherent));
    }


    @PatchMapping("/annuler/{inscriptionId}")
    @PreAuthorize("hasRole('ADHERENT')")
    public ResponseEntity<InscriptionResponse> annuler(
            @PathVariable @Parameter(description = "ID de l'inscription", required = true) Long inscriptionId,
            @AuthenticationPrincipal @Parameter(hidden = true) UserPrincipal principal) {

        Adherent adherent = getAdherentFromPrincipal(principal);
        return ResponseEntity.ok(inscriptionService.annuler(adherent, inscriptionId));
    }

    @GetMapping("/mesinscriptions")
    @PreAuthorize("hasRole('ADHERENT')")
    public ResponseEntity<List<InscriptionResponse>> mesInscriptions(
            @AuthenticationPrincipal @Parameter(hidden = true) UserPrincipal principal) {

        Adherent adherent = getAdherentFromPrincipal(principal);
        return ResponseEntity.ok(inscriptionService.mesInscriptions(adherent));
    }

    @PatchMapping("/confirmer/{inscriptionId}")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<InscriptionResponse> confirmer(
            @PathVariable @Parameter(description = "ID de l'inscription", required = true) Long inscriptionId) {
        return ResponseEntity.ok(inscriptionService.confirmer(inscriptionId));
    }

    @PatchMapping("/refuser/{inscriptionId}")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<InscriptionResponse> refuser(
            @PathVariable @Parameter(description = "ID de l'inscription", required = true) Long inscriptionId) {
        return ResponseEntity.ok(inscriptionService.refuser(inscriptionId));
    }

    @GetMapping("/offre/{offreId}")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<List<InscriptionResponse>> inscritsParOffre(
            @PathVariable @Parameter(description = "ID de l'offre", required = true) Long offreId) {
        return ResponseEntity.ok(inscriptionService.inscritsParOffre(offreId));
    }

    @PutMapping("/{id}/paiement")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<InscriptionResponse> mettreAjourPaiement(
            @PathVariable @Parameter(description = "ID de l'inscription", required = true) Long id,
            @RequestParam @Parameter(description = "Nouveau statut de paiement", required = true)
            StatutPaiement statut) {
        return ResponseEntity.ok(inscriptionService.mettreAjourPaiement(id, statut));
    }

    private Adherent getAdherentFromPrincipal(UserPrincipal principal) {
        Object user = userRepository
                .findByEmail(principal.getUsername())
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));

        if (!(user instanceof Adherent adherent)) {
            throw new EligibiliteException("Seuls les adhérents peuvent effectuer cette action");
        }
        return adherent;
    }

}