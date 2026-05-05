package tn.star.Pfe.dto.inscription;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestDTO {
    private String nom;
    private String prenom;
    private Integer age;
    private String sexe; // M / F / AUTRE
}
