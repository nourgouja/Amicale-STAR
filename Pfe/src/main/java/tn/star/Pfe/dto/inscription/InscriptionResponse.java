package tn.star.Pfe.dto.inscription;

import lombok.*;
import tn.star.Pfe.dto.paiement.EcheanceResponse;
import tn.star.Pfe.enums.ApprovalStatus;
import tn.star.Pfe.enums.PaymentStatus;

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
    private String typeOffre;
    private String periodePaiement;
    private Long adherentId;
    private String mailAdherent;
    private String adherentNom;
    private String adherentPrenom;
    private ApprovalStatus statut;
    private PaymentStatus statutPaiement;
    private BigDecimal montant;
    private LocalDateTime dateInscription;
    private LocalDateTime dateAnnulation;
    private int nombreAccompagnants;
    private Integer totalPeople;
    private java.math.BigDecimal prixParPersonne;
    private Integer placesRestantes;
    private List<EcheanceResponse> echeances;
    private List<GuestDTO> guests;

    public InscriptionResponse(Long id, String offreTitre, String mailAdherent, ApprovalStatus statut) {
        this.id = id;
        this.offreTitre = offreTitre;
        this.mailAdherent = mailAdherent;
        this.statut = statut;
    }
}
