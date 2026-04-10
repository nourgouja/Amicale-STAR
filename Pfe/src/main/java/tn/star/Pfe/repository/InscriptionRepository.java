package tn.star.Pfe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.user.Adherent;
import tn.star.Pfe.entity.Inscription;
import tn.star.Pfe.entity.Offre;
import tn.star.Pfe.enums.StatutInscription;
import tn.star.Pfe.enums.StatutPaiement;

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

    boolean existsByOffreAndAdherent(Offre offre, Adherent adherent);

    //dashboard
    long countByStatut(StatutInscription statut);

    long countByStatutPaiement(StatutPaiement statut);

    List<Inscription> findByStatutPaiement(StatutPaiement statut);
}