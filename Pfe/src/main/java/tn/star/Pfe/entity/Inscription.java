package tn.star.Pfe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tn.star.Pfe.enums.StatutInscription;
import tn.star.Pfe.enums.StatutPaiement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    private LocalDateTime dateInscription;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "adherent_id")
    private Adherent adherent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offre_id")
    private Offre offre;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutPaiement statutPaiement = StatutPaiement.EN_ATTENTE;
    private LocalDateTime datePaiement;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutInscription statut = StatutInscription.EN_ATTENTE;


    private LocalDateTime dateAnnulation;

    @Column(precision = 10, scale = 2)
    private BigDecimal montant;

    private String commentaire;

    @OneToMany(mappedBy = "inscription", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Echeance> echeances = new ArrayList<>();


}
