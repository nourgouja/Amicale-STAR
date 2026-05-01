package tn.star.Pfe.service.sondage;

import org.springframework.web.multipart.MultipartFile;
import tn.star.Pfe.dto.sondage.SondageRequest;
import tn.star.Pfe.dto.sondage.SondageResponse;
import tn.star.Pfe.dto.sondage.UpdateSondageRequest;

import java.io.IOException;
import java.util.List;

public interface ISondageService {

    SondageResponse creer(SondageRequest req, MultipartFile image1, MultipartFile image2, String username) throws IOException;

    SondageResponse modifier(Long id, UpdateSondageRequest req, MultipartFile image1, MultipartFile image2, String username) throws IOException;

    SondageResponse activer(Long id, String username);

    SondageResponse fermer(Long id, String username);

    SondageResponse archiver(Long id, String username);

    void supprimer(Long id, String username);

    List<SondageResponse> lister(String username);

    SondageResponse trouverParId(Long id, String username);

    SondageResponse voter(Long sondageId, Long optionId, String username);
}
