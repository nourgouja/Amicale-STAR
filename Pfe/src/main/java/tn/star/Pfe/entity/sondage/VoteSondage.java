package tn.star.Pfe.entity.sondage;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tn.star.Pfe.entity.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "vote_sondage", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sondage_id", "adherent_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VoteSondage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sondage_id", nullable = false)
    private Sondage sondage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adherent_id", nullable = false)
    private User adherent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private OptionSondage option;

    @CreationTimestamp
    private LocalDateTime votedAt;
}
