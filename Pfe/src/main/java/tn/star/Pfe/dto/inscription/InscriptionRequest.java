package tn.star.Pfe.dto.inscription;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class InscriptionRequest {

    @NotNull(message = "l'id de loffre est obligatoire")
    private Long offreId;
    private String maillAdherent;

}
