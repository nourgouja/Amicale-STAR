package tn.star.Pfe.service.election;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.star.Pfe.dto.election.candidate.AddCandidateRequest;
import tn.star.Pfe.dto.election.candidate.CandidateResponse;
import tn.star.Pfe.dto.election.candidate.CandidateSummary;
import tn.star.Pfe.dto.election.candidate.UserSummaryResponse;
import tn.star.Pfe.dto.election.election.ElectionResponse;
import tn.star.Pfe.dto.election.election.ElectionVoteRequest;
import tn.star.Pfe.entity.election.Candidate;
import tn.star.Pfe.entity.election.CandidateVote;
import tn.star.Pfe.entity.election.Election;
import tn.star.Pfe.entity.election.ElectionCall;
import tn.star.Pfe.entity.user.User;
import tn.star.Pfe.enums.PosteBureau;
import tn.star.Pfe.enums.StatutSondage;
import tn.star.Pfe.event.ElectionResultsPublishedEvent;
import tn.star.Pfe.event.ExtraRoundCreatedEvent;
import tn.star.Pfe.repository.election.CandidateRepository;
import tn.star.Pfe.repository.election.CandidateVoteRepository;
import tn.star.Pfe.repository.election.ElectionCallRepository;
import tn.star.Pfe.repository.election.ElectionRepository;
import tn.star.Pfe.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ElectionService implements IElectionService {

    private final ElectionRepository electionRepository;
    private final ElectionCallRepository electionCallRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateVoteRepository voteRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public ElectionResponse getElectionById(Long id, Long voterId) {
        Election election = electionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + id));
        return toResponse(election, voterId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ElectionResponse> getElectionsByCall(Long callId, Pageable pageable) {
        ElectionCall call = electionCallRepository.findById(callId)
                .orElseThrow(() -> new IllegalArgumentException("Election call not found: " + callId));

        return electionRepository.findByCall(call)
                .map(e -> (Page<ElectionResponse>) new PageImpl<>(List.of(toResponse(e, null)), pageable, 1))
                .orElseGet(() -> new PageImpl<>(List.of(), pageable, 0));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ElectionResponse> getAllActiveElections(Pageable pageable, Long voterId) {
        return electionRepository.findByStatus(StatutSondage.ACTIVE, pageable)
                .map(e -> toResponse(e, voterId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ElectionResponse> getAllClosedElections(Pageable pageable) {
        return electionRepository.findByStatus(StatutSondage.CLOSED, pageable)
                .map(e -> toResponse(e, null));
    }

    @Override
    @Transactional
    public ElectionResponse closeElection(Long id, Long userId) {
        log.info("Closing election {} by user {}", id, userId);
        Election election = electionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + id));
        if (election.getStatus() != StatutSondage.ACTIVE) {
            throw new IllegalStateException("Election is not active");
        }
        election.setStatus(StatutSondage.CLOSED);
        election.setUpdatedBy(userId);
        Election saved = electionRepository.save(election);
        log.info("Election {} closed", id);
        return toResponse(saved, null);
    }

    @Override
    @Transactional
    public void deleteElection(Long id, Long userId) {
        log.info("Deleting election {} by user {}", id, userId);
        Election election = electionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + id));
        voteRepository.deleteInBatch(voteRepository.findAllByElection(election.getId()));
        candidateRepository.deleteInBatch(candidateRepository.findByElection(election, Pageable.unpaged()).getContent());
        electionRepository.delete(election);
        log.info("Election {} deleted", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidateResponse> listCandidatesByPosition(Long electionId, PosteBureau position, Pageable pageable) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + electionId));

        return candidateRepository.findByElectionAndPosition(election, position, pageable)
                .map(this::toCandidateResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidateResponse> listAllCandidates(Long electionId, Pageable pageable) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + electionId));

        return candidateRepository.findByElection(election, pageable)
                .map(this::toCandidateResponse);
    }

    @Override
    @Transactional
    public CandidateResponse addCandidate(Long electionId, AddCandidateRequest request, Long userId) {
        log.info("Adding candidate {} to election {} by user {}", request.getUserId(), electionId, userId);

        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + electionId));

        if (election.getStatus() != StatutSondage.DRAFT) {
            throw new IllegalStateException("Cannot add candidates to non-draft election");
        }

        User candidateUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));

        if (candidateRepository.existsByElectionAndUserAndPosition(election, candidateUser, request.getPosition())) {
            throw new IllegalStateException("User is already a candidate for this position in this election");
        }

        Candidate candidate = Candidate.builder()
                .user(candidateUser)
                .election(election)
                .position(request.getPosition())
                .voteCount(0L)
                .build();

        Candidate saved = candidateRepository.save(candidate);
        log.info("Candidate added with id {}", saved.getId());
        return toCandidateResponse(saved);
    }

    @Override
    @Transactional
    public ElectionResponse voteForCandidate(ElectionVoteRequest request, Long voterId) {
        log.info("User {} voting for candidate {} in election {}", voterId, request.getCandidateId(), request.getElectionId());

        Election election = electionRepository.findById(request.getElectionId())
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + request.getElectionId()));

        User voter = userRepository.findById(voterId)
                .orElseThrow(() -> new IllegalArgumentException("Voter not found: " + voterId));

        if (!isUserEligibleToVote(voter)) {
            throw new IllegalStateException("You are not eligible to vote in this election");
        }

        if (election.getStatus() != StatutSondage.ACTIVE) {
            throw new IllegalStateException("Election is not active for voting");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(election.getDateDebut()) || now.isAfter(election.getDateFin())) {
            throw new IllegalStateException("Voting window has closed");
        }

        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found: " + request.getCandidateId()));

        if (!candidate.getElection().getId().equals(election.getId())) {
            throw new IllegalStateException("Candidate is not in this election");
        }

        PosteBureau position = candidate.getPosition();

        if (voteRepository.existsByElectionAndPositionAndVoter(election, position, voter)) {
            throw new IllegalStateException("You have already voted for this position in this election");
        }

        try {
            CandidateVote vote = CandidateVote.builder()
                    .election(election)
                    .candidate(candidate)
                    .position(position)
                    .voter(voter)
                    .build();

            voteRepository.save(vote);
            candidateRepository.incrementVoteCount(candidate.getId());

            log.info("Vote recorded for candidate {} by user {}", request.getCandidateId(), voterId);

        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate vote attempt for user {} in election {} for position {}", voterId, election.getId(), position);
            throw new IllegalStateException("You have already voted for this position in this election");
        }

        Election refreshed = electionRepository.findById(election.getId()).orElse(election);
        return toResponse(refreshed, voterId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getMyVotes(Long electionId, Long voterId) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + electionId));
        User voter = userRepository.findById(voterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + voterId));

        List<String> voted = new ArrayList<>();
        for (PosteBureau pos : PosteBureau.values()) {
            if (voteRepository.existsByElectionAndPositionAndVoter(election, pos, voter)) {
                voted.add(pos.name());
            }
        }
        return voted;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidateResponse> getElectionResults(Long electionId, Pageable pageable) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + electionId));

        return candidateRepository.findByElection(election, pageable)
                .map(this::toCandidateResponse);
    }

    @Override
    @Transactional
    public ElectionResponse publishResults(Long electionId, Long userId) {
        log.info("Publishing results for election {} by user {}", electionId, userId);

        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + electionId));

        if (election.getStatus() != StatutSondage.CLOSED) {
            throw new IllegalStateException("Cannot publish results for non-closed election");
        }

        election.setStatus(StatutSondage.RESULTS_PUBLISHED);
        election.setResultsPublishedAt(LocalDateTime.now());
        election.setUpdatedBy(userId);

        Election saved = electionRepository.save(election);
        eventPublisher.publishEvent(new ElectionResultsPublishedEvent(this, saved, userId, "{}"));

        log.info("Results published for election {}", electionId);
        return toResponse(saved, null);
    }

    @Override
    @Transactional
    public ElectionResponse createExtraRound(Long electionId, PosteBureau position, Long userId) {
        log.info("Creating extra round for election {} position {} by user {}", electionId, position, userId);

        Election parentElection = electionRepository.findById(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + electionId));

        if (parentElection.getStatus() != StatutSondage.RESULTS_PUBLISHED &&
                parentElection.getStatus() != StatutSondage.CLOSED) {
            throw new IllegalStateException("Extra round can only be created for closed elections");
        }

        List<Candidate> candidates = candidateRepository.findByElection(parentElection, Pageable.unpaged()).getContent();
        long maxVotes = candidates.stream()
                .filter(c -> c.getPosition() == position)
                .mapToLong(c -> c.getVoteCount() != null ? c.getVoteCount() : 0)
                .max()
                .orElse(0);

        Election extraRound = Election.builder()
                .titre(parentElection.getTitre() + " - Tour supplémentaire")
                .description(parentElection.getDescription())
                .status(StatutSondage.ACTIVE)
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusDays(2))
                .parentElection(parentElection)
                .createdBy(userId)
                .updatedBy(userId)
                .build();

        Election savedExtraRound = electionRepository.save(extraRound);

        Set<Long> copiedUserIds = new HashSet<>();
        for (Candidate orig : candidates) {
            if (orig.getPosition() == position &&
                    orig.getVoteCount() != null &&
                    orig.getVoteCount().equals(maxVotes) &&
                    !copiedUserIds.contains(orig.getUser().getId())) {

                Candidate newCandidate = Candidate.builder()
                        .user(orig.getUser())
                        .election(savedExtraRound)
                        .position(position)
                        .photo(orig.getPhoto())
                        .voteCount(0L)
                        .build();

                candidateRepository.save(newCandidate);
                copiedUserIds.add(orig.getUser().getId());
            }
        }

        eventPublisher.publishEvent(new ExtraRoundCreatedEvent(this, parentElection, savedExtraRound, userId));
        log.info("Extra round created with id {}", savedExtraRound.getId());
        return toResponse(savedExtraRound, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidateResponse> getAvailableUsers(Long electionId, Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> CandidateResponse.builder()
                        .id(user.getId())
                        .user(toUserSummaryResponse(user))
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public void validateElectionDates(Long electionId) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new IllegalArgumentException("Election not found: " + electionId));

        if (election.getDateFin().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Election deadline is in the past");
        }
    }

    private boolean isUserEligibleToVote(User voter) {
        return voter.isActif();
    }

    private ElectionResponse toResponse(Election election, Long voterId) {
        List<Candidate> allCandidates = candidateRepository.findByElection(election, Pageable.unpaged()).getContent();

        Map<PosteBureau, List<Candidate>> byPosition = allCandidates.stream()
                .collect(Collectors.groupingBy(Candidate::getPosition));

        List<CandidateSummary> summaries = new ArrayList<>();
        List<String> tiedPositions = new ArrayList<>();

        for (PosteBureau pos : PosteBureau.values()) {
            List<Candidate> posCandidates = byPosition.getOrDefault(pos, List.of());
            if (posCandidates.isEmpty()) continue;

            long totalForPosition = posCandidates.stream()
                    .mapToLong(c -> c.getVoteCount() != null ? c.getVoteCount() : 0).sum();

            long maxVotes = posCandidates.stream()
                    .mapToLong(c -> c.getVoteCount() != null ? c.getVoteCount() : 0).max().orElse(0);

            long countWithMax = posCandidates.stream()
                    .filter(c -> (c.getVoteCount() != null ? c.getVoteCount() : 0) == maxVotes)
                    .count();

            boolean hasTieForPos = maxVotes > 0 && countWithMax > 1;
            boolean autoSelected = posCandidates.size() == 1;

            if (hasTieForPos && (election.getStatus() == StatutSondage.CLOSED ||
                    election.getStatus() == StatutSondage.RESULTS_PUBLISHED)) {
                tiedPositions.add(pos.name());
            }

            for (Candidate c : posCandidates) {
                long votes = c.getVoteCount() != null ? c.getVoteCount() : 0;
                double pct = totalForPosition > 0 ? (double) votes / totalForPosition * 100 : 0;
                boolean winner = !hasTieForPos && votes > 0 && votes == maxVotes;

                summaries.add(CandidateSummary.builder()
                        .id(c.getId())
                        .prenom(c.getUser().getPrenom())
                        .nom(c.getUser().getNom())
                        .email(c.getUser().getEmail())
                        .position(pos.name())
                        .pictureUrl(c.getPhoto() != null ?
                                "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(c.getPhoto()) : null)
                        .voteCount(votes)
                        .votePercentage(pct)
                        .winner(winner || (autoSelected && votes == 0))
                        .autoSelected(autoSelected && votes == 0)
                        .build());
            }
        }

        summaries.sort(Comparator.comparing(CandidateSummary::getVoteCount).reversed());

        Map<String, List<CandidateSummary>> byPositionMap = summaries.stream()
                .collect(Collectors.groupingBy(CandidateSummary::getPosition));

        byPositionMap.values().forEach(list ->
                list.sort(Comparator.comparing(CandidateSummary::getVoteCount).reversed()));

        List<Election> extraRounds = electionRepository.findExtraRounds(election.getId());
        Long extraRoundElectionId = extraRounds.isEmpty() ? null : extraRounds.get(0).getId();

        List<String> votedPositions = new ArrayList<>();
        if (voterId != null) {
            User voter = userRepository.findById(voterId).orElse(null);
            if (voter != null) {
                for (PosteBureau pos : PosteBureau.values()) {
                    if (voteRepository.existsByElectionAndPositionAndVoter(election, pos, voter)) {
                        votedPositions.add(pos.name());
                    }
                }
            }
        }

        long totalVotes = voteRepository.countByElection(election);
        boolean closed = election.getStatus() == StatutSondage.CLOSED ||
                election.getStatus() == StatutSondage.RESULTS_PUBLISHED;

        return ElectionResponse.builder()
                .id(election.getId())
                .titre(election.getTitre())
                .description(election.getDescription())
                .status(election.getStatus())
                .dateDebut(election.getDateDebut())
                .dateFin(election.getDateFin())
                .closedAt(closed ? election.getDateFin() : null)
                .callId(election.getCall() != null ? election.getCall().getId() : null)
                .parentElectionId(election.getParentElection() != null ? election.getParentElection().getId() : null)
                .totalVotes(totalVotes)
                .totalCandidatesCount(allCandidates.size())
                .createdAt(election.getCreatedAt())
                .updatedAt(election.getUpdatedAt())
                .resultsPublishedAt(election.getResultsPublishedAt())
                .isResultsPublished(election.getStatus() == StatutSondage.RESULTS_PUBLISHED)
                .resultsPublished(election.getStatus() == StatutSondage.RESULTS_PUBLISHED)
                .canVote(election.getStatus() == StatutSondage.ACTIVE)
                .canPublishResults(election.getStatus() == StatutSondage.CLOSED)
                .candidates(summaries)
                .candidatesByPosition(byPositionMap)
                .votedPositions(votedPositions)
                .hasTie(!tiedPositions.isEmpty())
                .tiedPositions(tiedPositions)
                .isExtraRound(election.getParentElection() != null)
                .extraRoundElectionId(extraRoundElectionId)
                .build();
    }

    private CandidateResponse toCandidateResponse(Candidate candidate) {
        return CandidateResponse.builder()
                .id(candidate.getId())
                .user(toUserSummaryResponse(candidate.getUser()))
                .electionId(candidate.getElection().getId())
                .position(candidate.getPosition())
                .photo(candidate.getPhoto())
                .voteCount(candidate.getVoteCount())
                .createdAt(candidate.getCreatedAt())
                .build();
    }

    private UserSummaryResponse toUserSummaryResponse(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .firstName(user.getPrenom())
                .lastName(user.getNom())
                .email(user.getEmail())
                .phone(user.getTelephone())
                .build();
    }
}
