//package tn.star.Pfe.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import tn.star.Pfe.enums.StatutOffre;
//import tn.star.Pfe.enums.TypeOffre;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//@NoArgsConstructor
//@AllArgsConstructor
//@Data
//@Builder
//public class OffreResponse {
//    private int id;
//    private String titre;
//    private String description;
//    private TypeOffre typeOffre;
//    private StatutOffre statutOffre;
//    private LocalDate dateDebut;
//    private LocalDate dateFin;
//    private int capaciteMax;
//    private int placeRestantes;
//    private double prixParPersonne;
//    private String lieu;
//    private String imageURL;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//}
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

    private Long poleId;
    private String poleNom;
    private String avantages;
    private String societe;

    private ModePaiement modePaiement;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}