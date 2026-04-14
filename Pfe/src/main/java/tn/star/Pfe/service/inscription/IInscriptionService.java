package tn.star.Pfe.service.inscription;

import tn.star.Pfe.dto.inscription.InscriptionResponse;
import tn.star.Pfe.entity.Adherent;
import tn.star.Pfe.enums.StatutPaiement;

import java.util.List;

public interface IInscriptionService {
    InscriptionResponse inscrire(Long offreId, Adherent adherent);
    InscriptionResponse annuler(Adherent adherent, Long inscriptionId);
    InscriptionResponse confirmer(Long inscriptionId);
    List<InscriptionResponse> mesInscriptions(Adherent adherent);
    List<InscriptionResponse> inscritsParOffre(Long offreId);
    InscriptionResponse mettreAjourPaiement(Long inscriptionId, StatutPaiement statut);
}
