package edu.tcu.cs.projectpulse.peerevaluation;

import edu.tcu.cs.projectpulse.activeweek.ActiveWeek;
import edu.tcu.cs.projectpulse.user.AppUser;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "peer_evaluations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"evaluator_id", "evaluatee_id", "week_id"})
)
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"evaluator", "evaluatee", "week", "scores"})
public class PeerEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The student who filled out this evaluation
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private AppUser evaluator;

    // The student being evaluated (may be the same as evaluator for self-evaluation)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluatee_id", nullable = false)
    private AppUser evaluatee;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id", nullable = false)
    private ActiveWeek week;

    // Sent to the evaluatee (UC-28, UC-29)
    @Column(columnDefinition = "TEXT")
    private String publicComments;

    // Visible to instructor only — never shown to the evaluatee (UC-28, UC-31)
    @Column(columnDefinition = "TEXT")
    private String privateComments;

    // Whether the evaluator has finalised and submitted (BR-3: cannot edit once submitted)
    @Column(nullable = false)
    private boolean submitted = false;

    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "peerEvaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvaluationScore> scores = new ArrayList<>();
}
