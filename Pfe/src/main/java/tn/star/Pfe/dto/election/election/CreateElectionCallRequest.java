package tn.star.Pfe.dto.election.election;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateElectionCallRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String titre;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    @NotNull(message = "Application deadline is required")
    @FutureOrPresent(message = "Application deadline must be in the future")
    private LocalDateTime dateFinCandidature;

    @NotNull(message = "Vote start date is required")
    @Future(message = "Vote start date must be in the future")
    private LocalDateTime dateDebut;

    @NotNull(message = "Vote end date is required")
    @Future(message = "Vote end date must be in the future")
    private LocalDateTime dateFin;
}