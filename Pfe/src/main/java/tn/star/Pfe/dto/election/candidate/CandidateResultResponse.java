package tn.star.Pfe.dto.election.candidate;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateResultResponse {

    private Long candidateId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private byte[] photo;
    private Long voteCount;
    private Double votePercentage;
    private Integer rank;
    private boolean elected;
}
