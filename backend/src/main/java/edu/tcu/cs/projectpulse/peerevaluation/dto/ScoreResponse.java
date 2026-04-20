package edu.tcu.cs.projectpulse.peerevaluation.dto;

import edu.tcu.cs.projectpulse.peerevaluation.EvaluationScore;

public record ScoreResponse(
        Long criterionId,
        String criterionName,
        Integer score
) {
    public static ScoreResponse from(EvaluationScore s) {
        return new ScoreResponse(
                s.getCriterion().getId(),
                s.getCriterion().getName(),
                s.getScore()
        );
    }
}
