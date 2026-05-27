package tn.star.Pfe.repository.election;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.election.Candidate;
import tn.star.Pfe.entity.election.Election;
import tn.star.Pfe.entity.user.User;
import tn.star.Pfe.enums.PosteBureau;



@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    Page<Candidate> findByElection(Election election, Pageable pageable);

    Page<Candidate> findByElectionAndPosition(Election election, PosteBureau position, Pageable pageable);

    boolean existsByElectionAndUserAndPosition(Election election, User user, PosteBureau position);

    @Modifying
    @Query("UPDATE Candidate c SET c.voteCount = c.voteCount + 1 WHERE c.id = :candidateId")
    void incrementVoteCount(@Param("candidateId") Long candidateId);
}
