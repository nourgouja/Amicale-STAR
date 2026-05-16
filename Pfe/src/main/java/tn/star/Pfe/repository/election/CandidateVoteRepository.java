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
import java.util.Optional;

@Repository
public interface CandidateVoteRepository extends JpaRepository<CandidateVote, Long> {

    @Query("SELECT COUNT(cv) FROM CandidateVote cv WHERE cv.election.id = :electionId " +
            "AND cv.position = :position AND cv.voter.id = :voterId")
    long countVotesByVoterAndPosition(
            @Param("electionId") Long electionId,
            @Param("position") PosteBureau position,
            @Param("voterId") Long voterId);

    @Query("SELECT cv FROM CandidateVote cv WHERE cv.election.id = :electionId " +
            "AND cv.position = :position AND cv.voter.id = :voterId")
    Optional<CandidateVote> findExistingVote(
            @Param("electionId") Long electionId,
            @Param("position") PosteBureau position,
            @Param("voterId") Long voterId);

    @Query("SELECT COUNT(cv) FROM CandidateVote cv WHERE cv.candidate.id = :candidateId")
    long countVotesByCandidate(@Param("candidateId") Long candidateId);

    @Query("SELECT cv FROM CandidateVote cv WHERE cv.election.id = :electionId " +
            "ORDER BY cv.candidate.id, cv.createdAt")
    List<CandidateVote> findAllByElection(@Param("electionId") Long electionId);

    long countByElection(Election election);

    boolean existsByElectionAndPositionAndVoter(Election election, PosteBureau position, User voter);
}
