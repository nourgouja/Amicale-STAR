package tn.star.Pfe.dto.inscription;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestResponse {

    private Long id;
    private Long inscriptionId;
    private String nom;
    private String prenom;
    private Integer age;
    private String sexe;
    private LocalDateTime createdAt;
}
