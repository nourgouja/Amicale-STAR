package tn.star.Pfe.service.adhesion;

import tn.star.Pfe.dto.auth.create.DemandeAdhesionResponse;
import tn.star.Pfe.dto.auth.create.DemandeRequest;

import java.util.List;

public interface IAdhesionService {
    void soumettreDemande(DemandeRequest request);
    List<DemandeAdhesionResponse> getDemandesEnAttente();
    void approuver(Long id);
    void rejeter(Long id);
}
