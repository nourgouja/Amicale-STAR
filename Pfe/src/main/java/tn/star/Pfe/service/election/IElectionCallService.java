package tn.star.Pfe.service.election;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.star.Pfe.dto.election.candidate.ApplyRequest;
import tn.star.Pfe.dto.election.candidate.CandidateApplicationResponse;
import tn.star.Pfe.dto.election.election.CreateElectionCallRequest;
import tn.star.Pfe.dto.election.election.ElectionCallResponse;

import java.util.Optional;

public interface IElectionCallService {

    ElectionCallResponse createCall(CreateElectionCallRequest request, Long userId);

    ElectionCallResponse getCallById(Long id);

    Page<ElectionCallResponse> getAllOpenCalls(Pageable pageable);

    Page<ElectionCallResponse> getAllCalls(Pageable pageable);

    Optional<ElectionCallResponse> getActiveCall();

    ElectionCallResponse closeCall(Long id, Long userId);

    ElectionCallResponse updateCall(Long id, CreateElectionCallRequest request, Long userId);

    void deleteCall(Long id, Long userId);

    CandidateApplicationResponse applyAsCandidate(Long callId, ApplyRequest request, Long userId);

    Optional<CandidateApplicationResponse> getMyApplication(Long callId, Long userId);

    ElectionCallResponse publishElection(Long callId, Long userId);

    Page<ElectionCallResponse> searchCalls(String query, Pageable pageable);

    Page<CandidateApplicationResponse> getApplicationsForCall(Long callId, Pageable pageable);

    CandidateApplicationResponse approveApplication(Long callId, Long appId, Long adminId);

    CandidateApplicationResponse rejectApplication(Long callId, Long appId, String reason, Long adminId);
}
