package tn.star.Pfe.dto.election.election;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.star.Pfe.dto.election.candidate.CandidateSummary;
import tn.star.Pfe.enums.LifecycleStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElectionResponse {

    private Long id;
    private String titre;
    private String description;
    private LifecycleStatus status;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private LocalDateTime closedAt;
    private Long callId;
    private Long parentElectionId;
    private Long totalVotes;
    private long totalCandidatesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resultsPublishedAt;

    private List<CandidateSummary> candidates;
    private Map<String, List<CandidateSummary>> candidatesByPosition;
    private List<String> votedPositions;

    private boolean isResultsPublished;
    private boolean resultsPublished;
    private boolean hasTie;
    private List<String> tiedPositions;
    private boolean isExtraRound;
    private Long extraRoundElectionId;

    private boolean canVote;
    private boolean canPublishResults;
}
