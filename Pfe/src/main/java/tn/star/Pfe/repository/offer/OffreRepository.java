//package tn.star.Pfe.repository;
//
//import jakarta.persistence.Id;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//import tn.star.Pfe.entity.offre.Offre;
//import tn.star.Pfe.enums.OfferStatus;
//import tn.star.Pfe.enums.TypeOffre;
//
//import java.util.List;
//
//@Repository
//public interface OffreRepository extends JpaRepository<Offre, Integer> {
//    List<Offre> findByStatut(OfferStatus statutOffre);
//    List<Offre> findByType(TypeOffre typeOffre);
//
//    @Query( "select count(i) from Inscription i where i.offre.id = :offreId and i.statut = 'CONFIRMEE'")
//    int countInscritsConfirmes(int offreId);
//}
//
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

    List<Offre> findByStatutIn(List<OfferStatus> statuts);

    List<Offre> findByType(TypeOffre type);

    List<Offre> findByTitreContainingIgnoreCase(String titre);

    List<Offre> findByStatutAndType(OfferStatus statut, TypeOffre type);

    //dashboard
    long countByStatut(OfferStatus statut);

    @Query("SELECT COUNT(i) FROM Inscription i " +
            "WHERE i.offre.id = :offreId " +
            "AND i.statut = 'APPROVED'")
    int countInscritsConfirmes(@Param("offreId") Long offreId);

}