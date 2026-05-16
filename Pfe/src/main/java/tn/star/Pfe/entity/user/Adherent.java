package tn.star.Pfe.entity.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tn.star.Pfe.entity.inscription.Inscription;
import tn.star.Pfe.enums.ApprovalStatus;

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

    private String poste;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApprovalStatus statut = ApprovalStatus.APPROVED;

    //statut de ses paiements
}
