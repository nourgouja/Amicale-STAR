package tn.star.Pfe.repository.sondage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.sondage.VoteSondage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface VoteSondageRepository extends JpaRepository<VoteSondage, Long> {

    void deleteBySondageId(Long sondageId);

    Optional<VoteSondage> findBySondageIdAndAdherentId(Long sondageId, Long adherentId);

    boolean existsBySondageIdAndAdherentId(Long sondageId, Long adherentId);

    long countByOptionId(Long optionId);

    long countBySondageId(Long sondageId);

    @Query("SELECT v.option.id, COUNT(v) FROM VoteSondage v WHERE v.option.id IN :optionIds GROUP BY v.option.id")
    List<Object[]> countByOptionIdIn(@Param("optionIds") List<Long> optionIds);

    default Map<Long, Long> countsByOptionIds(List<Long> optionIds) {
        return countByOptionIdIn(optionIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }
}
