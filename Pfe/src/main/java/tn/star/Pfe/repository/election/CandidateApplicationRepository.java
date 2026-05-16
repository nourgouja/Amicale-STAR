package tn.star.Pfe.repository.election;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.election.CandidateApplication;
import tn.star.Pfe.entity.election.ElectionCall;
import tn.star.Pfe.entity.user.User;
import tn.star.Pfe.enums.PosteBureau;
import tn.star.Pfe.enums.StatutDemande;


import java.util.Optional;

@Repository
public interface CandidateApplicationRepository extends JpaRepository<CandidateApplication, Long> {
    Optional<CandidateApplication> findByUserAndCall(User user, ElectionCall call);

    Page<CandidateApplication> findByCall(ElectionCall call, Pageable pageable);

    Page<CandidateApplication> findByCallAndStatus(ElectionCall call, StatutDemande status, Pageable pageable);

    long countByCallAndStatus(ElectionCall call, StatutDemande status);

    long countByCallAndStatusAndPosition(ElectionCall call, StatutDemande status, PosteBureau position);

    boolean existsByUserAndCall(User user, ElectionCall call);
}