package tn.star.Pfe.dto.offre;

import lombok.*;
import tn.star.Pfe.enums.ModePaiement;
import tn.star.Pfe.enums.StatutOffre;
import tn.star.Pfe.enums.TypeOffre;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OffreResponse {

    private Long id;
    private String titre;
    private String description;
    private String lieu;
    private TypeOffre typeOffre;
    private StatutOffre statutOffre;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer capaciteMax;
    private Integer placesRestantes;
    private BigDecimal prixParPersonne;

    private String imageBase64;
    private String imageType;
    private String imageNom;

    private java.util.List<ImageSupplementaire> imagesSupplementaires;

    @lombok.Data @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class ImageSupplementaire {
        private Long id;
        private String base64;
        private String type;
        private String nom;
    }

    private Long poleId;
    private String poleNom;
    private String avantages;
    private String lienExterne;

    private ModePaiement modePaiement;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
