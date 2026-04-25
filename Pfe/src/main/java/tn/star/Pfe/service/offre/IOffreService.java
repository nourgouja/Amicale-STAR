package tn.star.Pfe.service.offre;

import org.springframework.web.multipart.MultipartFile;
import tn.star.Pfe.dto.offre.OffreRequest;
import tn.star.Pfe.dto.offre.OffreResponse;

import java.io.IOException;
import java.util.List;

public interface IOffreService {
    List<OffreResponse> listerOffresOuvertes();
    List<OffreResponse> listerToutesLesOffres();
    OffreResponse trouverParId(Long id);
    List<OffreResponse> rechercherParTitre(String titre);
    OffreResponse creer(OffreRequest req, MultipartFile image, String username) throws IOException;
    OffreResponse uploadImage(Long id, MultipartFile image);
    OffreResponse modifier(Long id, OffreRequest.UpdateOffreRequest req);
    OffreResponse modifierAvecImage(Long id, OffreRequest req, MultipartFile image) throws IOException;
    OffreResponse fermer(Long id);
    void supprimer(Long id);
    OffreResponse publier(Long id);
    OffreResponse archiver(Long id);
}
