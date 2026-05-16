package tn.star.Pfe.dto.election.candidate;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import tn.star.Pfe.enums.PosteBureau;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCandidateRequest {

    @NotNull(message = "Election ID is required")
    @Positive(message = "Election ID must be positive")
    private Long electionId;

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    @NotNull(message = "Position is required")
    private PosteBureau position;

    private MultipartFile photo;
}