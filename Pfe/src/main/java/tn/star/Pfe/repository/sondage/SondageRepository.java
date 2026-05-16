package tn.star.Pfe.repository.sondage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.sondage.Sondage;
import java.util.List;

@Repository
public interface SondageRepository extends JpaRepository<Sondage, Long> {

    List<Sondage> findAllByOrderByCreatedAtDesc();

    @Query("SELECT s FROM Sondage s WHERE s.statut IN ('OPEN', 'CLOSED') ORDER BY s.createdAt DESC")
    List<Sondage> findVisibleToAdherents();
}
