package tn.star.Pfe.dto.election.election;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.star.Pfe.enums.LifecycleStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElectionCallResponse {

    private Long id;
    private String titre;
    private String description;
    private LifecycleStatus status;
    private LocalDateTime dateFinCandidature;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private Long publishedElectionId;
    private String publishedElectionStatus;
    private Long approvedCandidatesCount;
    private Long totalApplicationsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean canApply;
    private boolean canPublish;
}
