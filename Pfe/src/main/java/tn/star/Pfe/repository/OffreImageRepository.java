package tn.star.Pfe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.OffreImage;

import java.util.List;

@Repository
public interface OffreImageRepository extends JpaRepository<OffreImage, Long> {
    List<OffreImage> findByOffreId(Long offreId);
}
