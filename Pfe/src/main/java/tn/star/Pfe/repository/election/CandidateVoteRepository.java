package tn.star.Pfe.repository.election;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.star.Pfe.entity.election.CandidateVote;
import tn.star.Pfe.entity.election.Election;
import tn.star.Pfe.entity.user.User;
import tn.star.Pfe.enums.PosteBureau;


import java.util.List;

@Repository
public interface CandidateVoteRepository extends JpaRepository<CandidateVote, Long> {

    @Query("SELECT cv FROM CandidateVote cv WHERE cv.election.id = :electionId " +
            "ORDER BY cv.candidate.id, cv.createdAt")
    List<CandidateVote> findAllByElection(@Param("electionId") Long electionId);

    long countByElection(Election election);

    boolean existsByElectionAndPositionAndVoter(Election election, PosteBureau position, User voter);
}
