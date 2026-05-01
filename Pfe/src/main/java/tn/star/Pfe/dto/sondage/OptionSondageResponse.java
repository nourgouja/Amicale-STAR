package tn.star.Pfe.dto.sondage;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OptionSondageResponse {

    private Long id;
    private String titre;
    private String description;
    private String imageBase64;
    private String imageType;
    private int ordre;
    private Long voteCount;
}
