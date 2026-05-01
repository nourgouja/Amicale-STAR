package tn.star.Pfe.dto.pole;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PoleRequest {

    @NotBlank
    private String nom;
}