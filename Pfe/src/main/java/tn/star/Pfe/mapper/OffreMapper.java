package tn.star.Pfe.mapper;

import org.springframework.stereotype.Component;
import tn.star.Pfe.dto.offre.OffreResponse;
import tn.star.Pfe.entity.Offre;

import java.util.Base64;


@Component
public class OffreMapper {

    public OffreResponse toResponse(Offre offre) {
        OffreResponse res = new OffreResponse();

        res.setId(offre.getId());
        res.setTitre(offre.getTitre());
        res.setDescription(offre.getDescription());
        res.setLieu(offre.getLieu());
        res.setTypeOffre(offre.getType());
        res.setStatutOffre(offre.getStatut());
        res.setDateDebut(offre.getDateDebut());
        res.setDateFin(offre.getDateFin());
        res.setCapaciteMax(offre.getCapaciteMax());
        res.setPlacesRestantes(offre.getPlacesRestantes());
        res.setPrixParPersonne(offre.getPrixParPersonne());
        res.setModePaiement(offre.getModePaiement());
        res.setAvantages(offre.getAvantages());
        res.setCreatedAt(offre.getCreatedAt());
        res.setUpdatedAt(offre.getUpdatedAt());

        if (offre.getPole() != null) {
            res.setPoleId(offre.getPole().getId());
            res.setPoleNom(offre.getPole().getNom());
        }

        if (offre.getImage() != null) {
            res.setImageBase64(Base64.getEncoder().encodeToString(offre.getImage())
            );
            res.setImageType(offre.getImageType());
            res.setImageNom(offre.getImageNom());
        }

        return res;
    }
}