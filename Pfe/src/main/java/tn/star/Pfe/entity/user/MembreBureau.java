package tn.star.Pfe.entity.user;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name="MembreBureau")
@DiscriminatorValue("MEMBRE_BUREAU")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class MembreBureau extends User {

    private String poste;

}
