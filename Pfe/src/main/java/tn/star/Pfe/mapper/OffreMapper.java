package tn.star.Pfe.mapper;

import org.springframework.stereotype.Component;
import tn.star.Pfe.dto.offre.OffreResponse;
import tn.star.Pfe.entity.offre.Offre;

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
        res.setAvantages(offre.getAvantages());
        res.setLienExterne(offre.getLienExterne());
        res.setCreatedAt(offre.getCreatedAt());
        res.setUpdatedAt(offre.getUpdatedAt());

        if (offre.getCreatedBy() != null) {
            tn.star.Pfe.entity.user.User cb = offre.getCreatedBy();
            String nom = ((cb.getPrenom() != null ? cb.getPrenom() : "") + " " + (cb.getNom() != null ? cb.getNom() : "")).trim();
            res.setCreatedByNom(nom.isEmpty() ? cb.getEmail() : nom);
        }

        if (offre.getPole() != null) {
            res.setPoleId(offre.getPole().getId());
            res.setPoleNom(offre.getPole().getNom());
        }

        if (offre.getImage() != null) {
            res.setImageBase64(Base64.getEncoder().encodeToString(offre.getImage()));
            res.setImageType(offre.getImageType());
            res.setImageNom(offre.getImageNom());
        }

        try {
            if (offre.getImagesSupplementaires() != null && !offre.getImagesSupplementaires().isEmpty()) {
                res.setImagesSupplementaires(
                    offre.getImagesSupplementaires().stream()
                        .map(img -> new OffreResponse.ImageSupplementaire(
                            img.getId(),
                            img.getData() != null ? Base64.getEncoder().encodeToString(img.getData()) : null,
                            img.getType(),
                            img.getNom()
                        ))
                        .toList()
                );
            }
        } catch (org.hibernate.LazyInitializationException ignored) {
        }

        return res;
    }
}