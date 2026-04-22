package edu.tcu.cs.projectpulse.report.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * One student's peer evaluation summary within a section-wide report (UC-31).
 * Includes private comments and evaluator identity — instructor-only view.
 */
public record StudentPeerSummary(
        Long studentId,
        String studentName,
        BigDecimal grade,
        boolean submitted,
        List<EvaluatorDetail> evaluations
) {
    public record EvaluatorDetail(
            Long evaluatorId,
            String evaluatorName,
            List<ScoreDetail> scores,
            String publicComments,
            String privateComments
    ) {}

    public record ScoreDetail(
            String criterionName,
            Integer score
    ) {}
}
