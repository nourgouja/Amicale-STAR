package tn.star.Pfe.entity.election;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tn.star.Pfe.entity.user.User;
import tn.star.Pfe.enums.PosteBureau;
import tn.star.Pfe.enums.ApprovalStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_application", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "call_id"}, name = "uk_user_call"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User cannot be null")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_app_user"))
    private User user;

    @NotNull(message = "Election call cannot be null")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "call_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_app_call"))
    private ElectionCall call;

    @NotNull(message = "PosteBureau cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PosteBureau position;

    @Column(columnDefinition = "TEXT")
    private String motivation;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus status;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] photo;

    @Column
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by_id")
    private Long reviewedBy;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version // for what purpose ?
    private Long version;
}
