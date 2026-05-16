package tn.star.Pfe.dto.election.candidate;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.star.Pfe.enums.PosteBureau;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateResponse {

    private Long id;
    private UserSummaryResponse user;
    private Long electionId;
    private PosteBureau position;
    private byte[] photo;
    private Long voteCount;
    private LocalDateTime createdAt;
}
