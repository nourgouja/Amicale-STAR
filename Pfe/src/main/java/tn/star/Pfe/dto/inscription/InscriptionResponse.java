package tn.star.Pfe.dto.inscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.star.Pfe.enums.StatutInscription;
import tn.star.Pfe.enums.StatutPaiement;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InscriptionResponse {

    private Long id;
    private Long offreId;
    private String offreTitre;
    private Long adherentId;
    private String mailAdherent;
    private StatutInscription statut;
    private StatutPaiement statutPaiement;
    private double montant;
    private LocalDateTime dateInscription;
    private LocalDateTime dateAnnulation;
    private String commentaire;


}
