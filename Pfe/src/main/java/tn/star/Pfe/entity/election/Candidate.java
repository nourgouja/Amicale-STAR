package tn.star.Pfe.entity.election;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.star.Pfe.entity.user.User;
import tn.star.Pfe.enums.PosteBureau;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "candidate", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "election_id", "position"}, name = "uk_candidate_position")) // comment ?
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User cannot be null")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_candidate_user"))
    private User user;

    @NotNull(message = "Election cannot be null")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "election_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_candidate_election"))
    private Election election;

    @NotNull(message = "PosteBureau cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PosteBureau position;

    @Column(columnDefinition = "TEXT")
    private String motivation;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] photo;

    @Column
    private Long voteCount;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CandidateVote> votes = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
