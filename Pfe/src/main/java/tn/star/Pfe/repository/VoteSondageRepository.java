package tn.star.Pfe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.VoteSondage;

import java.util.Optional;

@Repository
public interface VoteSondageRepository extends JpaRepository<VoteSondage, Long> {

    Optional<VoteSondage> findBySondageIdAndAdherentId(Long sondageId, Long adherentId);

    boolean existsBySondageIdAndAdherentId(Long sondageId, Long adherentId);

    long countByOptionId(Long optionId);

    long countBySondageId(Long sondageId);
}
