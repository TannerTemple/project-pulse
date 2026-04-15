package edu.tcu.cs.projectpulse.peerevaluation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeerEvaluationRepository extends JpaRepository<PeerEvaluation, Long> {

    // All evaluations submitted by a given evaluator in a given week
    List<PeerEvaluation> findByEvaluatorIdAndWeekId(Long evaluatorId, Long weekId);

    // All evaluations received by a given evaluatee in a given week
    List<PeerEvaluation> findByEvaluateeIdAndWeekId(Long evaluateeId, Long weekId);

    // All evaluations received by a student (across all weeks) — for the per-student report (UC-33)
    List<PeerEvaluation> findByEvaluateeId(Long evaluateeId);

    // Specific evaluator → evaluatee pair for a week (unique constraint)
    Optional<PeerEvaluation> findByEvaluatorIdAndEvaluateeIdAndWeekId(
            Long evaluatorId, Long evaluateeId, Long weekId);

    // All evaluations for an entire week (used for section-wide report, UC-31)
    List<PeerEvaluation> findByWeekId(Long weekId);

    void deleteByEvaluatorIdOrEvaluateeId(Long evaluatorId, Long evaluateeId);
}
