package tn.star.Pfe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tn.star.Pfe.enums.TypeOffre;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "pole")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pole_types_offre", joinColumns = @JoinColumn(name = "pole_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "type_offre", nullable = false)
//    @Builder.Default
//    private Set<TypeOffre> typesOffre = new HashSet<>();
    private List<TypeOffre> typesOffre;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
