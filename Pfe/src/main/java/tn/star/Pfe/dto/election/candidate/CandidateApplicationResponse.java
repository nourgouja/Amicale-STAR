package tn.star.Pfe.dto.election.candidate;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.star.Pfe.enums.StatutDemande;
import tn.star.Pfe.enums.PosteBureau;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateApplicationResponse {

    private Long id;
    private UserSummaryResponse user;
    private Long callId;
    private PosteBureau position;
    private String motivation;
    private byte[] photo;
    private StatutDemande status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String rejectionReason;
    private boolean canApply;
}
