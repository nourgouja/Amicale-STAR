package tn.star.Pfe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.OptionSondage;

import java.util.List;

@Repository
public interface OptionSondageRepository extends JpaRepository<OptionSondage, Long> {

    List<OptionSondage> findBySondageIdOrderByOrdre(Long sondageId);
}
