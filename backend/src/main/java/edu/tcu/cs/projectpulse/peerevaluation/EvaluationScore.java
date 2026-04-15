package edu.tcu.cs.projectpulse.peerevaluation;

import edu.tcu.cs.projectpulse.rubric.Criterion;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
    name = "evaluation_scores",
    uniqueConstraints = @UniqueConstraint(columnNames = {"peer_evaluation_id", "criterion_id"})
)
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"peerEvaluation", "criterion"})
public class EvaluationScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Scores must be integers per UC-28
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer score;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "peer_evaluation_id", nullable = false)
    private PeerEvaluation peerEvaluation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id", nullable = false)
    private Criterion criterion;
}
