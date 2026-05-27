package tn.star.Pfe.repository.sondage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.sondage.OptionSondage;


@Repository
public interface OptionSondageRepository extends JpaRepository<OptionSondage, Long> {


}
