package tn.star.Pfe.repository.inscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.user.Adherent;
import tn.star.Pfe.entity.inscription.Inscription;
import tn.star.Pfe.entity.offre.Offre;
import tn.star.Pfe.enums.StatutInscription;
import tn.star.Pfe.enums.TypeOffre;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    List<Inscription> findByOffre(Offre offre);

    List<Inscription> findByStatut(StatutInscription statut);

    List<Inscription> findByOffreAndStatut(Offre offre, StatutInscription statut);

    int countByOffreAndStatut(Offre offre, StatutInscription statut);

    Optional<Inscription> findByIdAndAdherent(Long id, Adherent adherent);

    List<Inscription> findByAdherent(Adherent adherent);

    boolean existsByOffreAndAdherentAndStatutNotIn(Offre offre, Adherent adherent, List<StatutInscription> statuts);

    //dashboard
    long countByStatut(StatutInscription statut);

    @Query("""
        SELECT i FROM Inscription i
        WHERE (:statut IS NULL OR i.statut = :statut)
          AND (:type IS NULL OR i.offre.type = :type)
          AND (:search IS NULL OR LOWER(i.adherent.nom) LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(i.adherent.prenom) LIKE LOWER(CONCAT('%',:search,'%')))
        ORDER BY i.dateInscription DESC
        """)
    List<Inscription> findFiltered(
        @Param("statut") StatutInscription statut,
        @Param("type") TypeOffre type,
        @Param("search") String search
    );
}