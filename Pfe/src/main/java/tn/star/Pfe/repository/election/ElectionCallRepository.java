package tn.star.Pfe.repository.election;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.election.ElectionCall;
import tn.star.Pfe.enums.LifecycleStatus;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ElectionCallRepository extends JpaRepository<ElectionCall, Long> {
    Page<ElectionCall> findByStatus(LifecycleStatus status, Pageable pageable);

    Optional<ElectionCall> findByIdAndStatus(Long id, LifecycleStatus status);

    Page<ElectionCall> findAllByStatusOrderByCreatedAtDesc(LifecycleStatus status, Pageable pageable);

    @Query("SELECT ec FROM ElectionCall ec WHERE ec.status = :status " +
            "AND ec.dateFinCandidature > :now ORDER BY ec.dateFinCandidature ASC")
    Page<ElectionCall> findOpenCallsBeforeDeadline(LifecycleStatus status, LocalDateTime now, Pageable pageable);

    @Query("SELECT ec FROM ElectionCall ec WHERE " +
            "LOWER(ec.titre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(ec.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY ec.createdAt DESC")
    Page<ElectionCall> searchByTitreOrDescription(@Param("query") String query, Pageable pageable);

    Optional<ElectionCall> findFirstByStatusOrderByCreatedAtDesc(LifecycleStatus status);

    Optional<ElectionCall> findFirstByPublishedElection_StatusOrderByCreatedAtDesc(LifecycleStatus publishedElectionStatus);
}