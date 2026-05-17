package tn.star.Pfe.service.election;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.star.Pfe.dto.election.candidate.ApplyRequest;
import tn.star.Pfe.dto.election.candidate.CandidateApplicationResponse;
import tn.star.Pfe.dto.election.candidate.UserSummaryResponse;
import tn.star.Pfe.dto.election.election.CreateElectionCallRequest;
import tn.star.Pfe.dto.election.election.ElectionCallResponse;
import tn.star.Pfe.entity.election.Candidate;
import tn.star.Pfe.entity.election.CandidateApplication;
import tn.star.Pfe.entity.election.Election;
import tn.star.Pfe.entity.election.ElectionCall;
import tn.star.Pfe.entity.user.User;
import tn.star.Pfe.enums.ApprovalStatus;
import tn.star.Pfe.enums.LifecycleStatus;
import tn.star.Pfe.enums.PosteBureau;
import tn.star.Pfe.event.ApplicationStatusChangedEvent;
import tn.star.Pfe.event.CandidacySubmittedEvent;
import tn.star.Pfe.event.ElectionCallCreatedEvent;
import tn.star.Pfe.event.ElectionPublishedEvent;
import tn.star.Pfe.repository.election.CandidateApplicationRepository;
import tn.star.Pfe.repository.election.CandidateRepository;
import tn.star.Pfe.repository.election.CandidateVoteRepository;
import tn.star.Pfe.repository.election.ElectionCallRepository;
import tn.star.Pfe.repository.election.ElectionRepository;
import tn.star.Pfe.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ElectionCallService implements IElectionCallService {

    private final ElectionCallRepository electionCallRepository;
    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateApplicationRepository applicationRepository;
    private final CandidateVoteRepository voteRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ElectionCallResponse createCall(CreateElectionCallRequest request, Long userId) {
        log.info("Creating election call by user {}", userId);

        ElectionCall call = ElectionCall.builder()
                .titre(request.getTitre())
                .description(request.getDescription())
                .status(LifecycleStatus.OPEN)
                .dateFinCandidature(request.getDateFinCandidature())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        ElectionCall saved = electionCallRepository.save(call);
        eventPublisher.publishEvent(new ElectionCallCreatedEvent(this, saved, userId));

        log.info("Election call created with id {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ElectionCallResponse getCallById(Long id) {
        ElectionCall call = electionCallRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Election call not found: " + id));
        return toResponse(call);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ElectionCallResponse> getAllOpenCalls(Pageable pageable) {
        return electionCallRepository.findByStatus(LifecycleStatus.OPEN, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ElectionCallResponse> getAllCalls(Pageable pageable) {
        return electionCallRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ElectionCallResponse> getActiveCall() {
        // Phase 1: candidacy open
        Optional<ElectionCall> openCall = electionCallRepository.findFirstByStatusOrderByCreatedAtDesc(LifecycleStatus.OPEN);
        if (openCall.isPresent()) return openCall.map(this::toResponse);
        // Phase 2: voting in progress (call closed, published election still open)
        Optional<ElectionCall> votingCall = electionCallRepository.findFirstByPublishedElection_StatusOrderByCreatedAtDesc(LifecycleStatus.OPEN);
        if (votingCall.isPresent()) return votingCall.map(this::toResponse);
        // Phase 3: results published
        return electionCallRepository.findFirstByPublishedElection_StatusOrderByCreatedAtDesc(LifecycleStatus.RESULTS_PUBLISHED)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public ElectionCallResponse closeCall(Long id, Long userId) {
        log.info("Closing election call {} by user {}", id, userId);
        ElectionCall call = electionCallRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Election call not found: " + id));
        if (call.getStatus() != LifecycleStatus.OPEN) {
            throw new IllegalStateException("Election call is not open");
        }
        call.setStatus(LifecycleStatus.CLOSED);
        call.setUpdatedBy(userId);
        return toResponse(electionCallRepository.save(call));
    }

    @Override
    @Transactional
    public ElectionCallResponse updateCall(Long id, CreateElectionCallRequest request, Long userId) {
        log.info("Updating election call {} by user {}", id, userId);

        ElectionCall call = electionCallRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Election call not found: " + id));

        call.setTitre(request.getTitre());
        call.setDescription(request.getDescription());
        call.setDateFinCandidature(request.getDateFinCandidature());
        call.setUpdatedBy(userId);

        ElectionCall updated = electionCallRepository.save(call);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCall(Long id, Long userId) {
        log.info("Deleting election call {} by user {}", id, userId);

        ElectionCall call = electionCallRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Election call not found: " + id));

        Election election = call.getPublishedElection();
        if (election != null) {
            voteRepository.deleteAllInBatch(voteRepository.findAllByElection(election.getId()));
            candidateRepository.deleteAllInBatch(candidateRepository.findByElection(election, Pageable.unpaged()).getContent());
            electionRepository.delete(election);
        }

        call.getCandidateApplications().forEach(applicationRepository::delete);
        electionCallRepository.delete(call);

        log.info("Election call {} deleted", id);
    }

    @Override
    @Transactional
    public CandidateApplicationResponse applyAsCandidate(Long callId, ApplyRequest request, Long userId) {
        log.info("User {} applying as candidate to call {}", userId, callId);

        ElectionCall call = electionCallRepository.findById(callId)
                .orElseThrow(() -> new IllegalArgumentException("Election call not found: " + callId));

        if (call.getStatus() != LifecycleStatus.OPEN) {
            throw new IllegalStateException("Election call is not open for applications");
        }

        if (LocalDateTime.now().isAfter(call.getDateFinCandidature())) {
            throw new IllegalStateException("Application deadline has passed");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (applicationRepository.existsByUserAndCall(user, call)) {
            throw new IllegalStateException("User has already applied for this election call");
        }

        byte[] photoBytes = null;
        if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
            try {
                photoBytes = request.getPhoto().getBytes();
            } catch (Exception e) {
                log.warn("Failed to read photo bytes for user {}", userId, e);
            }
        }

        CandidateApplication application = CandidateApplication.builder()
                .user(user)
                .call(call)
                .position(request.getPosition())
                .motivation(request.getMotivation())
                .poleNom(request.getPoleNom())
                .photo(photoBytes)
                .status(ApprovalStatus.PENDING)
                .build();

        CandidateApplication saved;
        try {
            saved = applicationRepository.save(application);
            eventPublisher.publishEvent(new CandidacySubmittedEvent(this, saved, userId));
            log.info("Candidacy submitted for user {} to call {}", userId, callId);
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate application attempt for user {} to call {}", userId, callId);
            throw new IllegalStateException("Application already exists for this call");
        }

        return toApplicationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CandidateApplicationResponse> getMyApplication(Long callId, Long userId) {
        ElectionCall call = electionCallRepository.findById(callId)
                .orElseThrow(() -> new IllegalArgumentException("Election call not found: " + callId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return applicationRepository.findByUserAndCall(user, call)
                .map(this::toApplicationResponse);
    }

    @Override
    @Transactional
    public ElectionCallResponse publishElection(Long callId, Long userId) {
        log.info("Publishing election for call {} by user {}", callId, userId);

        ElectionCall call = electionCallRepository.findById(callId)
                .orElseThrow(() -> new IllegalArgumentException("Election call not found: " + callId));

        if (call.getStatus() != LifecycleStatus.OPEN) {
            throw new IllegalStateException("Election call is not open");
        }

        long totalApproved = applicationRepository.countByCallAndStatus(call, ApprovalStatus.APPROVED);
        if (totalApproved == 0) {
            throw new IllegalStateException("Aucun candidat approuvé pour cette élection");
        }

        // Voting starts the moment the election is published, ends at the call's dateFin
        LocalDateTime voteStart = LocalDateTime.now();
        LocalDateTime voteEnd   = call.getDateFin() != null ? call.getDateFin() : voteStart.plusDays(30);

        Election election = Election.builder()
                .titre(call.getTitre())
                .description(call.getDescription())
                .status(LifecycleStatus.OPEN)
                .dateDebut(voteStart)
                .dateFin(voteEnd)
                .call(call)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        Election savedElection = electionRepository.save(election);

        // Create Candidate entities from approved applications
        List<CandidateApplication> approved = applicationRepository
                .findByCallAndStatus(call, ApprovalStatus.APPROVED, Pageable.unpaged()).getContent();
        for (CandidateApplication app : approved) {
            Candidate candidate = Candidate.builder()
                    .user(app.getUser())
                    .election(savedElection)
                    .position(app.getPosition())
                    .photo(app.getPhoto())
                    .voteCount(0L)
                    .build();
            candidateRepository.save(candidate);
        }

        call.setPublishedElection(savedElection);
        call.setStatus(LifecycleStatus.CLOSED);
        call.setUpdatedBy(userId);
        electionCallRepository.save(call);

        eventPublisher.publishEvent(new ElectionPublishedEvent(this, savedElection, call, userId));

        log.info("Election {} published for call {}", savedElection.getId(), callId);
        return toResponse(call);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ElectionCallResponse> searchCalls(String query, Pageable pageable) {
        return electionCallRepository.searchByTitreOrDescription(query, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidateApplicationResponse> getApplicationsForCall(Long callId, Pageable pageable) {
        ElectionCall call = electionCallRepository.findById(callId)
                .orElseThrow(() -> new IllegalArgumentException("Election call not found: " + callId));

        return applicationRepository.findByCall(call, pageable)
                .map(this::toApplicationResponse);
    }

    @Override
    @Transactional
    public CandidateApplicationResponse approveApplication(Long callId, Long appId, Long adminId) {
        log.info("Approving application {} for call {} by admin {}", appId, callId, adminId);

        CandidateApplication application = loadAndValidateApplication(callId, appId);

        if (application.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Application is not pending");
        }

        ApprovalStatus oldStatus = application.getStatus();
        application.setStatus(ApprovalStatus.APPROVED);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewedBy(adminId);

        CandidateApplication saved = applicationRepository.save(application);
        eventPublisher.publishEvent(new ApplicationStatusChangedEvent(this, saved, oldStatus, ApprovalStatus.APPROVED, adminId));

        log.info("Application {} approved", appId);
        return toApplicationResponse(saved);
    }

    @Override
    @Transactional
    public CandidateApplicationResponse rejectApplication(Long callId, Long appId, String reason, Long adminId) {
        log.info("Rejecting application {} for call {} by admin {}", appId, callId, adminId);

        CandidateApplication application = loadAndValidateApplication(callId, appId);

        if (application.getStatus() != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Application is not pending");
        }

        ApprovalStatus oldStatus = application.getStatus();
        application.setStatus(ApprovalStatus.REJECTED);
        application.setReviewedAt(LocalDateTime.now());
        application.setReviewedBy(adminId);
        application.setRejectionReason(reason);

        CandidateApplication saved = applicationRepository.save(application);
        eventPublisher.publishEvent(new ApplicationStatusChangedEvent(this, saved, oldStatus, ApprovalStatus.REJECTED, adminId));

        log.info("Application {} rejected", appId);
        return toApplicationResponse(saved);
    }

    private CandidateApplication loadAndValidateApplication(Long callId, Long appId) {
        CandidateApplication application = applicationRepository.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + appId));

        if (!application.getCall().getId().equals(callId)) {
            throw new IllegalArgumentException("Application does not belong to call: " + callId);
        }
        return application;
    }

    private ElectionCallResponse toResponse(ElectionCall call) {
        long approvedCount = applicationRepository.countByCallAndStatus(call, ApprovalStatus.APPROVED);
        long totalCount = call.getCandidateApplications().size();

        return ElectionCallResponse.builder()
                .id(call.getId())
                .titre(call.getTitre())
                .description(call.getDescription())
                .status(call.getStatus())
                .dateFinCandidature(call.getDateFinCandidature())
                .dateDebut(call.getDateDebut())
                .dateFin(call.getDateFin())
                .publishedElectionId(call.getPublishedElection() != null ? call.getPublishedElection().getId() : null)
                .publishedElectionStatus(call.getPublishedElection() != null ? call.getPublishedElection().getStatus().name() : null)
                .approvedCandidatesCount(approvedCount)
                .totalApplicationsCount(totalCount)
                .createdAt(call.getCreatedAt())
                .updatedAt(call.getUpdatedAt())
                .canApply(call.getStatus() == LifecycleStatus.OPEN &&
                        LocalDateTime.now().isBefore(call.getDateFinCandidature()))
                .canPublish(call.getStatus() == LifecycleStatus.OPEN && approvedCount > 0)
                .build();
    }

    private CandidateApplicationResponse toApplicationResponse(CandidateApplication app) {
        User user = app.getUser();
        return CandidateApplicationResponse.builder()
                .id(app.getId())
                .user(UserSummaryResponse.builder()
                        .id(user.getId())
                        .firstName(user.getPrenom())
                        .lastName(user.getNom())
                        .email(user.getEmail())
                        .phone(user.getTelephone())
                        .build())
                .callId(app.getCall().getId())
                .position(app.getPosition())
                .motivation(app.getMotivation())
                .poleNom(app.getPoleNom())
                .photo(app.getPhoto())
                .status(app.getStatus())
                .createdAt(app.getCreatedAt())
                .reviewedAt(app.getReviewedAt())
                .rejectionReason(app.getRejectionReason())
                .canApply(app.getStatus() == ApprovalStatus.PENDING)
                .build();
    }
}
