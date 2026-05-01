package tn.star.Pfe.dto.sondage;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSondageRequest {

    @Size(min = 5, max = 200, message = "Le titre doit contenir entre 5 et 200 caractères")
    private String titre;

    private OptionUpdateRequest option1;

    private OptionUpdateRequest option2;

    @Data
    public static class OptionUpdateRequest {

        @Size(max = 200)
        private String titre;

        private String description;
    }
}
