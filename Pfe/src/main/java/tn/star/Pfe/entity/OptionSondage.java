package tn.star.Pfe.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "option_sondage")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OptionSondage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sondage_id", nullable = false)
    private Sondage sondage;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(name = "image", columnDefinition = "LONGBLOB")
    private byte[] image;

    private String imageNom;
    private String imageType;

    private int ordre;
}
