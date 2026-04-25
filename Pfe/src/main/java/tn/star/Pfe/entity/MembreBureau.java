package tn.star.Pfe.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import tn.star.Pfe.enums.PosteBureau;
import tn.star.Pfe.enums.TypeOffre;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="MembreBureau")
@DiscriminatorValue("MEMBRE_BUREAU")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class MembreBureau extends User {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pole_id")
    private Pole pole;

    @Column
    @Enumerated(EnumType.STRING)
    private PosteBureau poste;

    @ElementCollection(targetClass = TypeOffre.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "membre_bureau_types_autorisees", joinColumns = @JoinColumn(name = "membre_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "type_offre")
    @Builder.Default
    private Set<TypeOffre> typesAutorisees = new HashSet<>();
}
