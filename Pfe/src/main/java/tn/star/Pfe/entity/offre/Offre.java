package tn.star.Pfe.entity.offre;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.star.Pfe.entity.inscription.Inscription;
import tn.star.Pfe.entity.user.Adherent;
import tn.star.Pfe.entity.user.Pole;
import tn.star.Pfe.enums.ApprovalStatus;
import tn.star.Pfe.enums.OfferStatus;
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

    @Column(columnDefinition = "TEXT")
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


    @ManyToOne
    @JoinColumn(name = "adherent_id")
    private Adherent adherent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private tn.star.Pfe.entity.user.User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pole_id")
    private Pole pole;
    @Column(columnDefinition = "TEXT")
    private String avantages;

    private String lienExterne;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus statut = OfferStatus.DRAFT;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OffreImage> imagesSupplementaires = new ArrayList<>();

    //@JsonIgnore recherche chnw hedha
    @OneToMany(mappedBy= "offre" , cascade = CascadeType.ALL , orphanRemoval = true,fetch = FetchType.EAGER)
    @Builder.Default
    private List<Inscription> inscriptions = new ArrayList<>();


    public Integer getPlacesRestantes(){
        if (capaciteMax == 0) return null; // no capacity limit set
        int occupied = inscriptions.stream()
                .filter(i -> i.getStatut() == ApprovalStatus.APPROVED)
                .mapToInt(Inscription::getTotalPeople)
                .sum();
        return capaciteMax - occupied;
    }

}
