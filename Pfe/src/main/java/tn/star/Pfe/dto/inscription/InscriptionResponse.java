package tn.star.Pfe.dto.inscription;

import lombok.*;
import tn.star.Pfe.dto.paiement.EcheanceResponse;
import tn.star.Pfe.enums.StatutInscription;
import tn.star.Pfe.enums.StatutPaiement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder @Getter @Setter
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
    private BigDecimal montant;
    private LocalDateTime dateInscription;
    private LocalDateTime dateAnnulation;
    private String commentaire;
    private List<EcheanceResponse> echeances;

    public InscriptionResponse(Long id, String offreTitre, String mailAdherent, StatutInscription statut) {
        this.id = id;
        this.offreTitre = offreTitre;
        this.mailAdherent = mailAdherent;
        this.statut = statut;
    }


}
