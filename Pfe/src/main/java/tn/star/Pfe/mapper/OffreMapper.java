package tn.star.Pfe.mapper;

import org.springframework.stereotype.Component;
import tn.star.Pfe.dto.offre.OffreResponse;
import tn.star.Pfe.entity.Offre;


@Component
public class OffreMapper {

    public OffreResponse toResponse(Offre o) {
        return OffreResponse.builder()
                .id(o.getId())
                .titre(o.getTitre())
                .description(o.getDescription())
                .typeOffre(o.getType())
                .statutOffre(o.getStatut())
                .dateDebut(o.getDateDebut())
                .dateFin(o.getDateFin())
                .capaciteMax(o.getCapaciteMax())
                .placeRestantes(o.getPlacesRestantes())
                .prixParPersonne(o.getPrixParPersonne())
                .lieu(o.getLieu())
                .imageType(o.getImageType())
                .imageNom(o.getImageNom())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}