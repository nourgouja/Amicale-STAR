//package tn.star.Pfe.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//import tn.star.Pfe.dto.inscription.InscriptionResponse;
//import tn.star.Pfe.entity.user.Adherent;
//import tn.star.Pfe.enums.StatutPaiement;
//import tn.star.Pfe.exceptions.EligibiliteException;
//import tn.star.Pfe.exceptions.NotFoundException;
//import tn.star.Pfe.repository.user.UserRepository;
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
import tn.star.Pfe.dto.inscription.InscriptionCreateRequest;
import tn.star.Pfe.dto.inscription.InscriptionResponse;
import tn.star.Pfe.entity.user.Adherent;
import tn.star.Pfe.enums.PeriodePaiement;
import tn.star.Pfe.enums.StatutInscription;
import tn.star.Pfe.enums.TypeOffre;
import tn.star.Pfe.exceptions.EligibiliteException;
import tn.star.Pfe.exceptions.NotFoundException;
import tn.star.Pfe.repository.user.UserRepository;
import tn.star.Pfe.security.UserPrincipal;
import tn.star.Pfe.service.inscription.IInscriptionService;

import java.util.List;

@RestController
@RequestMapping("/api/inscriptions")
@RequiredArgsConstructor
public class InscriptionController {

    private final IInscriptionService inscriptionService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<List<InscriptionResponse>> listerToutes() {
        return ResponseEntity.ok(inscriptionService.listerToutesInscriptions());
    }

    @GetMapping("/bureau")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<List<InscriptionResponse>> listerFiltrees(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search) {
        StatutInscription statutEnum = null;
        TypeOffre typeEnum = null;
        try { if (statut != null && !statut.isBlank()) statutEnum = StatutInscription.valueOf(statut); } catch (IllegalArgumentException ignored) {}
        try { if (type   != null && !type.isBlank())   typeEnum   = TypeOffre.valueOf(type);           } catch (IllegalArgumentException ignored) {}
        return ResponseEntity.ok(inscriptionService.listerFiltrees(statutEnum, typeEnum, search));
    }

    @PostMapping("/inscrire/{offreId}")
    @PreAuthorize("hasRole('ADHERENT')")
    public ResponseEntity<InscriptionResponse> inscrire(
            @PathVariable @Parameter(description = "ID de l'offre", required = true, example = "1") Long offreId,
            @RequestBody(required = false) InscriptionCreateRequest body,
            @AuthenticationPrincipal @Parameter(hidden = true) UserPrincipal principal) {

        Adherent adherent = getAdherentFromPrincipal(principal);
        InscriptionCreateRequest req = body != null ? body : new InscriptionCreateRequest();

        PeriodePaiement periodePaiement = PeriodePaiement.COMPTANT;
        if (req.getPaymentPeriod() != null && !req.getPaymentPeriod().isBlank()) {
            try {
                periodePaiement = PeriodePaiement.valueOf(req.getPaymentPeriod().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new tn.star.Pfe.exceptions.BadRequestException(
                    "Période de paiement invalide. Valeurs acceptées : COMPTANT, MENSUEL, TRIMESTRIEL, SEMESTRIEL");
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inscriptionService.inscrire(offreId, adherent, req.getGuests(), periodePaiement));
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

    // ── GET single inscription ────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBRE_BUREAU', 'ADMIN')")
    public ResponseEntity<InscriptionResponse> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(inscriptionService.getById(id));
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