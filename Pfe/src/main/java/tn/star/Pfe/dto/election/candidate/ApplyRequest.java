package tn.star.Pfe.dto.election.candidate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class ApplyRequest {

    @NotNull(message = "Position is required")
    private PosteBureau position;

    @NotBlank(message = "Motivation is required")
    @Size(min = 10, max = 1000, message = "Motivation must be between 10 and 1000 characters")
    private String motivation;

    private MultipartFile photo;
}
