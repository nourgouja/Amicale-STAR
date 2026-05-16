package tn.star.Pfe.entity.election;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import tn.star.Pfe.entity.user.User;

import tn.star.Pfe.enums.PosteBureau;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_vote", uniqueConstraints = @UniqueConstraint(columnNames = {"election_id", "position", "voter_id"}, name = "uk_vote_per_position_per_voter"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Election cannot be null")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "election_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_vote_election"))
    private Election election;

    @NotNull(message = "Candidate cannot be null")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "candidate_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vote_candidate"))
    private Candidate candidate;

    @NotNull(message = "Position cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PosteBureau position;

    @NotNull(message = "Voter cannot be null")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "voter_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vote_voter"))
    private User voter;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;
}
