package tn.star.Pfe.repository.offer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.offre.OffreImage;

import java.util.List;

// to remove
@Repository
public interface OffreImageRepository extends JpaRepository<OffreImage, Long> {
    List<OffreImage> findByOffreId(Long offreId);
}
