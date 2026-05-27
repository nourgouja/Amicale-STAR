package tn.star.Pfe.repository.inscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.star.Pfe.entity.user.Adherent;
import tn.star.Pfe.entity.inscription.Echeance;
import tn.star.Pfe.entity.inscription.Inscription;

import tn.star.Pfe.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface EcheanceRepository extends JpaRepository<Echeance, Long> {

    List<Echeance> findByInscription(Inscription inscription);

    List<Echeance> findByInscription_Adherent(Adherent adherent);

    List<Echeance> findByStatutAndDateEcheanceBefore(PaymentStatus statut, LocalDate date);

    List<Echeance> findByDateEcheanceBeforeAndStatut(LocalDate date, PaymentStatus statut);

    List<Echeance> findByStatutIn(List<PaymentStatus> statuts);

    List<Echeance> findByDateEcheanceBetweenAndStatut(LocalDate start, LocalDate end, PaymentStatus statut);

    long countByStatut(PaymentStatus statut);

    @Query("SELECT COALESCE(SUM(e.montant), 0) FROM Echeance e WHERE e.statut = :statut")
    BigDecimal sumMontantByStatut(@Param("statut") PaymentStatus statut);

}
