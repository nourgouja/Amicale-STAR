package tn.star.Pfe.service.adhesion;

import tn.star.Pfe.dto.auth.DemandeAdhesionResponse;
import tn.star.Pfe.dto.auth.DemandeRequest;

import java.util.List;

public interface IAdhesionService {
    void soumettreDemande(DemandeRequest request);
    List<DemandeAdhesionResponse> getDemandesEnAttente();
    void approuver(Long id);
    void rejeter(Long id);
}
