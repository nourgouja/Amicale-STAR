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
public class CreateElectionRequest {

    @NotNull(message = "Election call ID is required")
    private Long callId;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String titre;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be in the future")
    private LocalDateTime dateDebut;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDateTime dateFin;

    @AssertTrue(message = "Start date must be before end date")
    private boolean isDateOrder() {
        if (dateDebut == null || dateFin == null) {
            return true;
        }
        return dateDebut.isBefore(dateFin);
    }
}
