package tn.star.Pfe.dto.sondage;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SondageResponse {

    private Long id;
    private String titre;
    private String statut;
    private String createdByNom;
    private String createdByPrenom;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    private List<OptionSondageResponse> options;
    private boolean hasVoted;
    private Long votedOptionId;
    private long totalVotes;
}
