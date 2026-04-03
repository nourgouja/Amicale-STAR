//package tn.star.Pfe.repository;
//
//import jakarta.persistence.Id;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//import tn.star.Pfe.entity.Offre;
//import tn.star.Pfe.enums.StatutOffre;
//import tn.star.Pfe.enums.TypeOffre;
//
//import java.util.List;
//
//@Repository
//public interface OffreRepository extends JpaRepository<Offre, Integer> {
//    List<Offre> findByStatut(StatutOffre statutOffre);
//    List<Offre> findByType(TypeOffre typeOffre);
//
//    @Query( "select count(i) from Inscription i where i.offre.id = :offreId and i.statut = 'CONFIRMEE'")
//    int countInscritsConfirmes(int offreId);
//}
//
package tn.star.Pfe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.Offre;
import tn.star.Pfe.enums.StatutOffre;
import tn.star.Pfe.enums.TypeOffre;

import java.util.List;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Integer> {

    List<Offre> findByStatut(StatutOffre statut);

    List<Offre> findByType(TypeOffre type);

    List<Offre> findByTitreContainingIgnoreCase(String titre);

    List<Offre> findByStatutAndType(StatutOffre statut, TypeOffre type);

    //dashboard
    long countByStatut(StatutOffre statut);

    @Query("SELECT COUNT(i) FROM Inscription i " +
            "WHERE i.offre.id = :offreId " +
            "AND i.statut = 'CONFIRMEE'")
    int countInscritsConfirmes(@Param("offreId") int offreId);
}