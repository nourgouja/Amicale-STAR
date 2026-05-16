package tn.star.Pfe.entity.election;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.star.Pfe.enums.LifecycleStatus;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "election")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Election {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Titre cannot be blank")
    @Column(nullable = false)
    private String titre;

    @NotBlank(message = "Description cannot be blank")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LifecycleStatus status;

    @NotNull(message = "Voting start date cannot be null")
    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @NotNull(message = "Voting end date cannot be null")
    @Column(nullable = false)
    private LocalDateTime dateFin;

    @ManyToOne
    @JoinColumn(name = "call_id", foreignKey = @ForeignKey(name = "fk_election_call"))
    private ElectionCall call;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_election_id",
            foreignKey = @ForeignKey(name = "fk_election_parent"))
    @NotFound(action = NotFoundAction.IGNORE)
    private Election parentElection;

    @OneToMany(mappedBy = "parentElection", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Election> extraRounds = new ArrayList<>();

    @OneToMany(mappedBy = "election", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Candidate> candidates = new ArrayList<>();

    @OneToMany(mappedBy = "election", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CandidateVote> votes = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String resultsJson;

    @Column
    private LocalDateTime resultsPublishedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by_id")
    private Long createdBy;

    @Column(name = "updated_by_id")
    private Long updatedBy;

    @Version
    private Long version;
}
