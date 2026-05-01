package tn.star.Pfe.dto.sondage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SondageRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 200, message = "Le titre doit contenir entre 5 et 200 caractères")
    private String titre;

    @Valid
    @NotNull(message = "L'option 1 est obligatoire")
    private OptionRequest option1;

    @Valid
    @NotNull(message = "L'option 2 est obligatoire")
    private OptionRequest option2;

    @Data
    public static class OptionRequest {

        @NotBlank(message = "Le titre de l'option est obligatoire")
        @Size(max = 200)
        private String titre;

        @NotBlank(message = "La description de l'option est obligatoire")
        private String description;
    }
}
