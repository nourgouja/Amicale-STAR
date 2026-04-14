package tn.star.Pfe.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import tn.star.Pfe.enums.PosteBureau;

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

}
