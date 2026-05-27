package tn.star.Pfe.repository.offer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.offre.Offre;
import tn.star.Pfe.enums.OfferStatus;
import tn.star.Pfe.enums.TypeOffre;

import java.util.List;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {

    List<Offre> findByStatut(OfferStatus statut);

    List<Offre> findByTitreContainingIgnoreCase(String titre);

    @Query("SELECT o FROM Offre o WHERE o.statut IN :statuts OR o.type = :type")
    List<Offre> findByStatutInOrType(@Param("statuts") List<OfferStatus> statuts, @Param("type") TypeOffre type);
}