package tn.star.Pfe.dto.sondage;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoteRequest {

    @NotNull(message = "L'identifiant de l'option est obligatoire")
    private Long optionId;
}
