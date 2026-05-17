package tn.star.Pfe.service.inscription;

import tn.star.Pfe.dto.inscription.GuestDTO;
import tn.star.Pfe.dto.inscription.InscriptionResponse;
import tn.star.Pfe.entity.user.Adherent;
import tn.star.Pfe.enums.PeriodePaiement;
import tn.star.Pfe.enums.ApprovalStatus;
import tn.star.Pfe.enums.TypeOffre;

import java.util.List;

public interface IInscriptionService {
    InscriptionResponse inscrire(Long offreId, Adherent adherent, List<GuestDTO> guests, PeriodePaiement periodePaiement);
    InscriptionResponse annuler(Adherent adherent, Long inscriptionId);
    InscriptionResponse confirmer(Long inscriptionId);
    InscriptionResponse refuser(Long inscriptionId);
    List<InscriptionResponse> mesInscriptions(Adherent adherent);
    List<InscriptionResponse> inscritsParOffre(Long offreId);
    List<InscriptionResponse> listerToutesInscriptions();
    List<InscriptionResponse> listerFiltrees(ApprovalStatus statut, TypeOffre type, String search);
    InscriptionResponse getById(Long inscriptionId);
    InscriptionResponse modifier(Adherent adherent, Long inscriptionId, List<GuestDTO> guests, PeriodePaiement periodePaiement);
}
