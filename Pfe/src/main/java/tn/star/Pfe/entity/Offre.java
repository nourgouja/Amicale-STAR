package tn.star.Pfe.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.star.Pfe.enums.ModePaiement;
import tn.star.Pfe.enums.StatutInscription;
import tn.star.Pfe.enums.StatutOffre;
import tn.star.Pfe.enums.TypeOffre;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "offre")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private String lieu;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Column(precision = 10, scale = 2)
    private BigDecimal prixParPersonne;
    private int capaciteMax;



    @Enumerated(EnumType.STRING)
    private TypeOffre type;

    @Lob // a recherche lob
    @Column(name = "image", columnDefinition = "LONGBLOB")
    private byte[] image;

    private String imageNom;
    private String imageType;


    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // par qui
    @ManyToOne
    @JoinColumn(name = "adherent_id")
    private Adherent adherent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pole_id")
    private Pole pole;
    private String avantages;

    @Enumerated(EnumType.STRING)
    private ModePaiement modePaiement;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutOffre statut = StatutOffre.BROUILLON;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OffreImage> imagesSupplementaires = new ArrayList<>();

    //@JsonIgnore
    @OneToMany(mappedBy= "offre" , cascade = CascadeType.ALL , orphanRemoval = true,fetch = FetchType.EAGER)
    @Builder.Default
    private List<Inscription> inscriptions = new ArrayList<>();


    public int getPlacesRestantes(){
        long confirme = inscriptions.stream().filter(i->i.getStatut()== StatutInscription.CONFIRMEE).count();
        return capaciteMax-(int)confirme;
    }
}
