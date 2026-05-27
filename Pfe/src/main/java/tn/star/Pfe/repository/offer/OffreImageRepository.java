package tn.star.Pfe.repository.offer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.offre.OffreImage;


@Repository
public interface OffreImageRepository extends JpaRepository<OffreImage, Long> {
}
