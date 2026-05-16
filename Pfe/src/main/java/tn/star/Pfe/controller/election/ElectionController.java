package tn.star.Pfe.controller.election;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tn.star.Pfe.dto.election.candidate.AddCandidateRequest;
import tn.star.Pfe.dto.election.candidate.CandidateResponse;
import tn.star.Pfe.dto.election.election.ElectionResponse;
import tn.star.Pfe.dto.election.election.ElectionVoteRequest;
import tn.star.Pfe.enums.PosteBureau;
import tn.star.Pfe.security.UserPrincipal;
import tn.star.Pfe.service.election.IElectionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/elections")
@RequiredArgsConstructor
@Slf4j
public class ElectionController {

    private final IElectionService electionService;

    // ==================== READ - STATIC ROUTES FIRST ====================

    @GetMapping("/closed")
    public ResponseEntity<Page<ElectionResponse>> getAllClosedElections(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /elections/closed");
        return ResponseEntity.ok(electionService.getAllClosedElections(pageable));
    }

    @GetMapping("/available-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CandidateResponse>> getAvailableUsers(
            @RequestParam Long electionId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /elections/available-users electionId={}", electionId);
        return ResponseEntity.ok(electionService.getAvailableUsers(electionId, pageable));
    }

    // ==================== READ - GENERIC ROUTES ====================

    @GetMapping
    public ResponseEntity<Page<ElectionResponse>> getAllActiveElections(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("GET /elections");
        Long voterId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(electionService.getAllActiveElections(pageable, voterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ElectionResponse> getElection(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("GET /elections/{}", id);
        Long voterId = principal != null ? principal.getId() : null;
        return ResponseEntity.ok(electionService.getElectionById(id, voterId));
    }

    // ==================== ADMIN ACTIONS ====================

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ElectionResponse> closeElection(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("POST /elections/{}/close by admin={}", id, principal.getId());
        return ResponseEntity.ok(electionService.closeElection(id, principal.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteElection(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("DELETE /elections/{} by admin={}", id, principal.getId());
        electionService.deleteElection(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    // ==================== CANDIDATES ====================

    @GetMapping("/{electionId}/candidates")
    public ResponseEntity<Page<CandidateResponse>> listAllCandidates(
            @PathVariable Long electionId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /elections/{}/candidates", electionId);
        return ResponseEntity.ok(electionService.listAllCandidates(electionId, pageable));
    }

    @GetMapping("/{electionId}/candidates/by-position/{position}")
    public ResponseEntity<Page<CandidateResponse>> listCandidatesByPosition(
            @PathVariable Long electionId,
            @PathVariable PosteBureau position,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /elections/{}/candidates/by-position/{}", electionId, position);
        return ResponseEntity.ok(electionService.listCandidatesByPosition(electionId, position, pageable));
    }

    @PostMapping("/{electionId}/candidates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CandidateResponse> addCandidate(
            @PathVariable Long electionId,
            @Valid @ModelAttribute AddCandidateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("POST /elections/{}/candidates userId={} by admin={}", electionId, request.getUserId(), principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(electionService.addCandidate(electionId, request, principal.getId()));
    }

    // ==================== VOTING ====================

    @PostMapping("/{electionId}/vote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ElectionResponse> voteForCandidate(
            @PathVariable Long electionId,
            @Valid @RequestBody ElectionVoteRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("POST /elections/{}/vote candidateId={} by userId={}", electionId, request.getCandidateId(), principal.getId());
        return ResponseEntity.ok(electionService.voteForCandidate(request, principal.getId()));
    }

    @GetMapping("/{electionId}/my-votes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> getMyVotes(
            @PathVariable Long electionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("GET /elections/{}/my-votes by userId={}", electionId, principal.getId());
        return ResponseEntity.ok(electionService.getMyVotes(electionId, principal.getId()));
    }

    // ==================== RESULTS ====================

    @GetMapping("/{electionId}/results")
    public ResponseEntity<Page<CandidateResponse>> getResults(
            @PathVariable Long electionId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /elections/{}/results", electionId);
        return ResponseEntity.ok(electionService.getElectionResults(electionId, pageable));
    }

    @PostMapping("/{electionId}/publish-results")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ElectionResponse> publishResults(
            @PathVariable Long electionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("POST /elections/{}/publish-results by admin={}", electionId, principal.getId());
        return ResponseEntity.ok(electionService.publishResults(electionId, principal.getId()));
    }

    @PostMapping("/{electionId}/extra-round/{position}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ElectionResponse> createExtraRound(
            @PathVariable Long electionId,
            @PathVariable PosteBureau position,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("POST /elections/{}/extra-round/{} by admin={}", electionId, position, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(electionService.createExtraRound(electionId, position, principal.getId()));
    }

    @PostMapping("/{electionId}/validate-dates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> validateDates(@PathVariable Long electionId) {
        log.info("POST /elections/{}/validate-dates", electionId);
        electionService.validateElectionDates(electionId);
        return ResponseEntity.ok().build();
    }
}
