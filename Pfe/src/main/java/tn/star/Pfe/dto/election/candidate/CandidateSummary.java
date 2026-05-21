package tn.star.Pfe.dto.election.candidate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateSummary {
    private Long id;
    private String prenom;
    private String nom;
    private String email;
    private String position;
    private String pictureUrl;
    private String motivation;
    private Long voteCount;
    private double votePercentage;
    private boolean winner;
    private boolean autoSelected;
}
