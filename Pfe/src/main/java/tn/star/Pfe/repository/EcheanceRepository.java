package tn.star.Pfe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.star.Pfe.entity.Adherent;
import tn.star.Pfe.entity.Echeance;
import tn.star.Pfe.entity.Inscription;
import tn.star.Pfe.entity.Pole;
import tn.star.Pfe.enums.StatutPaiement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface EcheanceRepository extends JpaRepository<Echeance, Long> {

    List<Echeance> findByInscription(Inscription inscription);

    List<Echeance> findByInscription_Adherent(Adherent adherent);

    List<Echeance> findByStatutAndDateEcheanceBefore(StatutPaiement statut, LocalDate date);

    long countByStatut(StatutPaiement statut);

    @Query("SELECT COALESCE(SUM(e.montant), 0) FROM Echeance e WHERE e.statut = :statut")
    BigDecimal sumMontantByStatut(@Param("statut") StatutPaiement statut);

    @Query("SELECT COALESCE(SUM(e.montant), 0) FROM Echeance e " + "WHERE e.statut = :statut AND e.inscription.offre.pole = :pole")
    BigDecimal sumMontantByPoleAndStatut(@Param("pole") Pole pole, @Param("statut") StatutPaiement statut);

    List<Echeance> findByDateEcheanceBeforeAndStatut(LocalDate date, StatutPaiement statut);

}
