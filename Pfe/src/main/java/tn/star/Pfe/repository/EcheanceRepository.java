package tn.star.Pfe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.star.Pfe.entity.user.Adherent;
import tn.star.Pfe.entity.inscription.Echeance;
import tn.star.Pfe.entity.inscription.Inscription;
import tn.star.Pfe.entity.user.Pole;
import tn.star.Pfe.enums.StatutPaiement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface EcheanceRepository extends JpaRepository<Echeance, Long> {

    List<Echeance> findByInscription(Inscription inscription);

    List<Echeance> findByInscription_Adherent(Adherent adherent);

    List<Echeance> findByStatutAndDateEcheanceBefore(StatutPaiement statut, LocalDate date);

    List<Echeance> findByDateEcheanceBeforeAndStatut(LocalDate date, StatutPaiement statut);

    List<Echeance> findByStatutIn(List<StatutPaiement> statuts);

    List<Echeance> findByStatutOrderByDateEcheanceAsc(StatutPaiement statut);

    List<Echeance> findByDateEcheanceBetweenAndStatut(LocalDate start, LocalDate end, StatutPaiement statut);

    List<Echeance> findByDateEcheanceAndStatut(LocalDate date, StatutPaiement statut);

    long countByStatut(StatutPaiement statut);

    @Query("SELECT COALESCE(SUM(e.montant), 0) FROM Echeance e WHERE e.statut = :statut")
    BigDecimal sumMontantByStatut(@Param("statut") StatutPaiement statut);

    @Query("SELECT COALESCE(SUM(e.montant), 0) FROM Echeance e " +
           "WHERE e.statut = :statut AND e.inscription.offre.pole = :pole")
    BigDecimal sumMontantByPoleAndStatut(@Param("pole") Pole pole, @Param("statut") StatutPaiement statut);
}
