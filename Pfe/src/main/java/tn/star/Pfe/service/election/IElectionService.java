package tn.star.Pfe.service.election;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.star.Pfe.dto.election.candidate.AddCandidateRequest;
import tn.star.Pfe.dto.election.candidate.CandidateResponse;
import tn.star.Pfe.dto.election.election.ElectionResponse;
import tn.star.Pfe.dto.election.election.ElectionVoteRequest;
import tn.star.Pfe.enums.PosteBureau;

import java.util.List;

public interface IElectionService {

    ElectionResponse getElectionById(Long id, Long voterId);

    Page<ElectionResponse> getElectionsByCall(Long callId, Pageable pageable);

    Page<ElectionResponse> getAllActiveElections(Pageable pageable, Long voterId);

    Page<ElectionResponse> getAllClosedElections(Pageable pageable);

    ElectionResponse closeElection(Long id, Long userId);

    Page<CandidateResponse> listCandidatesByPosition(Long electionId, PosteBureau position, Pageable pageable);

    Page<CandidateResponse> listAllCandidates(Long electionId, Pageable pageable);

    CandidateResponse addCandidate(Long electionId, AddCandidateRequest request, Long userId);

    ElectionResponse voteForCandidate(ElectionVoteRequest request, Long voterId);

    List<String> getMyVotes(Long electionId, Long voterId);

    Page<CandidateResponse> getElectionResults(Long electionId, Pageable pageable);

    ElectionResponse publishResults(Long electionId, Long userId);

    ElectionResponse createExtraRound(Long electionId, PosteBureau position, Long userId);

    Page<CandidateResponse> getAvailableUsers(Long electionId, Pageable pageable);

    void validateElectionDates(Long electionId);

    void deleteElection(Long id, Long userId);
}
