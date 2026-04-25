package tn.star.Pfe.mapper;

import org.springframework.stereotype.Component;
import tn.star.Pfe.dto.paiement.EcheanceResponse;
import tn.star.Pfe.dto.inscription.InscriptionResponse;
import tn.star.Pfe.entity.Echeance;
import tn.star.Pfe.entity.Inscription;

import java.util.Collections;
import java.util.List;

@Component
public class InscriptionMapper {

    public InscriptionResponse toResponse(Inscription i) {
        return InscriptionResponse.builder()
                .id(i.getId())
                .offreId(i.getOffre().getId())
                .offreTitre(i.getOffre().getTitre())
                .adherentId(i.getAdherent().getId())
                .mailAdherent(i.getAdherent().getEmail())
                .adherentNom(i.getAdherent().getNom())
                .adherentPrenom(i.getAdherent().getPrenom())
                .statut(i.getStatut())
                .montant(i.getMontant())
                .statutPaiement(i.getStatutPaiement())
                .dateInscription(i.getDateInscription())
                .dateAnnulation(i.getDateAnnulation())
                .commentaire(i.getCommentaire())
                .echeances(mapEcheances(i))
                .build();
    }

    private List<EcheanceResponse> mapEcheances(Inscription i) {
        if (i.getEcheances() == null || i.getEcheances().isEmpty())
            return Collections.emptyList();

        return i.getEcheances().stream()
                .map(e -> new EcheanceResponse(
                        e.getId(),
                        i.getId(),
                        e.getNumero(),
                        e.getMontant(),
                        e.getDateEcheance(),
                        e.getStatut()))
                .toList();
    }
}