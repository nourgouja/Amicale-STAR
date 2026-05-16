package tn.star.Pfe.dto.election.election;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.star.Pfe.enums.PosteBureau;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElectionVoteRequest {

    @NotNull(message = "Election ID is required")
    @Positive(message = "Election ID must be positive")
    private Long electionId;

    @NotNull(message = "Candidate ID is required")
    @Positive(message = "Candidate ID must be positive")
    private Long candidateId;

    @NotNull(message = "Position is required")
    private PosteBureau position;
}
