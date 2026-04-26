package tn.star.Pfe.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "offre_image")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OffreImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offre_id", nullable = false)
    private Offre offre;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] data;

    private String nom;
    private String type;
}
