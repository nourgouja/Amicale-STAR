package tn.star.Pfe.dto.pole;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import tn.star.Pfe.enums.TypeOffre;

@Getter @Setter
public class PoleRequest{
    @NotBlank
    private String nom;
    @NotNull
    private TypeOffre typeOffre;
}
