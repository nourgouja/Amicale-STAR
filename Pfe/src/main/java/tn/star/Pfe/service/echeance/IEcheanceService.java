package tn.star.Pfe.service.echeance;

import tn.star.Pfe.dto.paiement.EcheanceResponse;

import java.util.List;

public interface IEcheanceService {
    List<EcheanceResponse> parInscription(Long inscriptionId);
    EcheanceResponse marquerPayee(Long echeanceId);
    void marquerEnRetard();
}
