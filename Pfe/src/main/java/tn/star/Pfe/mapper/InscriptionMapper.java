package tn.star.Pfe.mapper;

import org.springframework.stereotype.Component;
import tn.star.Pfe.dto.inscription.InscriptionResponse;
import tn.star.Pfe.entity.Inscription;


@Component
public class InscriptionMapper {

    public InscriptionResponse toResponse(Inscription i, Object o) {
        return InscriptionResponse.builder()
                .id(i.getId())
                .offreId(i.getOffre().getId())
                .offreTitre(i.getOffre().getTitre())
                .adherentId(i.getAdherentId())
                .mailAdherent(i.getMailAdherent())
                .statut(i.getStatut())
                .montant(i.getMontant())
                .dateInscription(i.getDateInscription())
                .dateAnnulation(i.getDateAnnulation())
                .commentaire(i.getCommentaire())
                .build();
    }
}