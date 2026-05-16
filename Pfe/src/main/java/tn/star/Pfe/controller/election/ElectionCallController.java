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
import tn.star.Pfe.dto.election.candidate.ApplyRequest;
import tn.star.Pfe.dto.election.candidate.CandidateApplicationResponse;
import tn.star.Pfe.dto.election.election.CreateElectionCallRequest;
import tn.star.Pfe.dto.election.election.ElectionCallResponse;
import tn.star.Pfe.security.UserPrincipal;
import tn.star.Pfe.service.election.IElectionCallService;

@RestController
@RequestMapping("/api/v1/election-calls")
@RequiredArgsConstructor
@Slf4j
public class ElectionCallController {

    private final IElectionCallService electionCallService;

    // ==================== CREATE ====================
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ElectionCallResponse> createElectionCall(
            @Valid @RequestBody CreateElectionCallRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("POST /election-calls by admin={}", principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(electionCallService.createCall(request, principal.getId()));
    }

    // ==================== READ - SPECIFIC ROUTES (BEFORE /{id}) ====================
    @GetMapping("/active")
    public ResponseEntity<ElectionCallResponse> getActiveCall() {
        log.info("GET /election-calls/active");
        return electionCallService.getActiveCall()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ElectionCallResponse>> searchCalls(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /election-calls/search?query={}", query);
        return ResponseEntity.ok(electionCallService.searchCalls(query, pageable));
    }

    // ==================== READ - ADMIN ALL CALLS ====================
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ElectionCallResponse>> getAllCalls(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /election-calls/all");
        return ResponseEntity.ok(electionCallService.getAllCalls(pageable));
    }

    // ==================== READ - GENERIC ROUTES ====================
    @GetMapping
    public ResponseEntity<Page<ElectionCallResponse>> getAllOpenCalls(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /election-calls (page={}, size={})", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(electionCallService.getAllOpenCalls(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ElectionCallResponse> getElectionCall(@PathVariable Long id) {
        log.info("GET /election-calls/{}", id);
        return ResponseEntity.ok(electionCallService.getCallById(id));
    }

    // ==================== UPDATE ====================
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ElectionCallResponse> updateElectionCall(
            @PathVariable Long id,
            @Valid @RequestBody CreateElectionCallRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("PUT /election-calls/{} by admin={}", id, principal.getId());
        return ResponseEntity.ok(electionCallService.updateCall(id, request, principal.getId()));
    }

    // ==================== DELETE ====================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteElectionCall(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("DELETE /election-calls/{} by admin={}", id, principal.getId());
        electionCallService.deleteCall(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    // ==================== ELECTION CALL ACTIONS ====================
    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ElectionCallResponse> closeCall(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("POST /election-calls/{}/close by admin={}", id, principal.getId());
        return ResponseEntity.ok(electionCallService.closeCall(id, principal.getId()));
    }

    @PostMapping("/{callId}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ElectionCallResponse> publishElection(
            @PathVariable Long callId,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("POST /election-calls/{}/publish by admin={}", callId, principal.getId());
        return ResponseEntity.ok(electionCallService.publishElection(callId, principal.getId()));
    }

    // ==================== CANDIDATE APPLICATIONS ====================
    @PostMapping("/{callId}/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CandidateApplicationResponse> applyAsCandidate(
            @PathVariable Long callId,
            @Valid @ModelAttribute ApplyRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("POST /election-calls/{}/apply by userId={}", callId, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(electionCallService.applyAsCandidate(callId, request, principal.getId()));
    }

    @GetMapping("/{callId}/my-application")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CandidateApplicationResponse> getMyApplication(
            @PathVariable Long callId,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("GET /election-calls/{}/my-application by userId={}", callId, principal.getId());
        return electionCallService.getMyApplication(callId, principal.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{callId}/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CandidateApplicationResponse>> getApplications(
            @PathVariable Long callId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /election-calls/{}/applications", callId);
        return ResponseEntity.ok(electionCallService.getApplicationsForCall(callId, pageable));
    }

    @PostMapping("/{callId}/applications/{appId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CandidateApplicationResponse> approveApplication(
            @PathVariable Long callId,
            @PathVariable Long appId,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("POST /election-calls/{}/applications/{}/approve by admin={}", callId, appId, principal.getId());
        return ResponseEntity.ok(electionCallService.approveApplication(callId, appId, principal.getId()));
    }

    @PostMapping("/{callId}/applications/{appId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CandidateApplicationResponse> rejectApplication(
            @PathVariable Long callId,
            @PathVariable Long appId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("POST /election-calls/{}/applications/{}/reject by admin={}", callId, appId, principal.getId());
        return ResponseEntity.ok(electionCallService.rejectApplication(callId, appId, reason, principal.getId()));
    }
}