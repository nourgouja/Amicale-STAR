package tn.star.Pfe.dto.offre;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import tn.star.Pfe.enums.ModePaiement;
import tn.star.Pfe.enums.TypeOffre;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class OffreRequest {

    @NotBlank
    private String titre;

    private String description;

    private String lieu;

    @NotNull
    private TypeOffre typeOffre;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    @DecimalMin(value = "0.0")
    private BigDecimal prixParPersonne;

    @Min(1)
    private Integer capaciteMax;

    private ModePaiement modePaiement;

    private String avantages;
    private Long poleId;

    @Getter
    @Setter
    public static class UpdateOffreRequest {
        private String titre;
        private String description;
        private TypeOffre typeOffre;
        private LocalDate dateDebut;
        private LocalDate dateFin;
        private Integer capaciteMax;
        private BigDecimal prixParPersonne;
        private String lieu;
        private Long agenceId;
        private ModePaiement modePaiement;
        private String logoUrl;
        private String avantages;
    }
}