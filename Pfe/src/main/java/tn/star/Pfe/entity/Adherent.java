package tn.star.Pfe.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tn.star.Pfe.enums.StatutDemande;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "adherent")
@DiscriminatorValue("ADHERENT")
@Getter @Setter
@NoArgsConstructor
@SuperBuilder
public class Adherent extends User {

    @OneToMany(mappedBy = "adherent", fetch = FetchType.EAGER)
    @Builder.Default
    private List<Inscription> inscriptions = new ArrayList<>();

    @Builder.Default
    private boolean aDette = false;

    @Builder.Default
    private double soldeCongé = 0.0;

    private String matriculeStar;

    private String poste;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutDemande statut = StatutDemande.APPROVED;
}
