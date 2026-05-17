package tn.star.Pfe.entity.inscription;

import jakarta.persistence.*;
import lombok.*;
import tn.star.Pfe.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "echeance")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Echeance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscription_id", nullable = false)
    private Inscription inscription;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false)
    private LocalDate dateEcheance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    @Builder.Default
    private PaymentStatus statut = PaymentStatus.PENDING;

    @Column
    private LocalDateTime datePaiement;

    public boolean isOverdue() {
        return statut == PaymentStatus.PENDING && LocalDate.now().isAfter(dateEcheance);
    }

    public int getDaysUntilDue() {
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), dateEcheance);
    }
}
