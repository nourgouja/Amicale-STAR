package tn.star.Pfe.dto.inscription;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GuestRequest {

    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    private Integer age;

    private String sexe;
}
