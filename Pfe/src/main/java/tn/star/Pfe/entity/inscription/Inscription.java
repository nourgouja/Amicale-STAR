package tn.star.Pfe.entity.inscription;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Convert;
import tn.star.Pfe.dto.inscription.GuestDTO;
import tn.star.Pfe.entity.offre.Offre;
import tn.star.Pfe.entity.user.Adherent;
import tn.star.Pfe.enums.PeriodePaiement;
import tn.star.Pfe.enums.ApprovalStatus;
import tn.star.Pfe.enums.PaymentStatus;

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

    @Column(columnDefinition = "VARCHAR(50)")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApprovalStatus statut = ApprovalStatus.PENDING;

    private LocalDateTime dateAnnulation;

    @Column(precision = 10, scale = 2)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "periode_paiement", columnDefinition = "VARCHAR(50)")
    private PeriodePaiement periodePaiement;

    private int nombreAccompagnants;

    @OneToMany(mappedBy = "inscription", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Echeance> echeances = new ArrayList<>();

    @Convert(converter = GuestListConverter.class)
    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private List<GuestDTO> guests = new ArrayList<>();

    public int getTotalPeople() {
        return 1 + (guests != null ? guests.size() : 0);
    }

    public PaymentStatus getStatutPaiement() {
        if (echeances == null || echeances.isEmpty()) return PaymentStatus.PENDING;
        long paid = echeances.stream()
                .filter(e -> e.getStatut() == PaymentStatus.PAID)
                .count();
        if (paid == echeances.size()) return PaymentStatus.PAID;
        long overdue = echeances.stream()
                .filter(e -> e.getStatut() == PaymentStatus.OVERDUE)
                .count();
        if (overdue > 0) return PaymentStatus.OVERDUE;
        return PaymentStatus.PENDING;
    }

    public int getPaidCount() {
        if (echeances == null) return 0;
        return (int) echeances.stream()
                .filter(e -> e.getStatut() == PaymentStatus.PAID)
                .count();
    }

    public int getOverdueCount() {
        if (echeances == null) return 0;
        return (int) echeances.stream()
                .filter(Echeance::isOverdue)
                .count();
    }
}
