package tn.star.Pfe.entity.election;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.star.Pfe.enums.LifecycleStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "election_call")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElectionCall {
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

    @NotNull(message = "Application deadline cannot be null")
    @Column(nullable = false)
    private LocalDateTime dateFinCandidature;

    @Column
    private LocalDateTime dateDebut;

    @Column
    private LocalDateTime dateFin;

    @OneToOne(mappedBy = "call", cascade = CascadeType.ALL, orphanRemoval = true)
    private Election publishedElection;

    @OneToMany(mappedBy = "call", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CandidateApplication> candidateApplications = new ArrayList<>();

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
