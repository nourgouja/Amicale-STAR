package tn.star.Pfe.repository.election;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.election.Election;
import tn.star.Pfe.entity.election.ElectionCall;
import tn.star.Pfe.enums.LifecycleStatus;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ElectionRepository extends JpaRepository<Election, Long> {
    Optional<Election> findByCall(ElectionCall call);

    Page<Election> findByStatus(LifecycleStatus status, Pageable pageable);

    Optional<Election> findByIdAndStatus(Long id, LifecycleStatus status);

    @Query("SELECT e FROM Election e WHERE e.status = :status AND e.dateFin < :now")
    Page<Election> findClosedElections(
            @Param("status") LifecycleStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Query("SELECT e FROM Election e WHERE e.status IN ('OPEN', 'CLOSED') " +
            "AND e.dateFin < :now AND e.resultsJson IS NULL")
    List<Election> findElectionsNeedingResults(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM Election e WHERE e.parentElection.id = :parentId")
    List<Election> findExtraRounds(@Param("parentId") Long parentId);

    long countByCall(ElectionCall call);
}
