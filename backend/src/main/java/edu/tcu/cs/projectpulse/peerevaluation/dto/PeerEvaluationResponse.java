package edu.tcu.cs.projectpulse.peerevaluation.dto;

import edu.tcu.cs.projectpulse.peerevaluation.PeerEvaluation;

import java.time.LocalDateTime;
import java.util.List;

public record PeerEvaluationResponse(
        Long id,
        Long evaluatorId,
        String evaluatorName,
        Long evaluateeId,
        String evaluateeName,
        Long weekId,
        String publicComments,
        String privateComments,
        boolean submitted,
        LocalDateTime submittedAt,
        List<ScoreResponse> scores
) {
    public static PeerEvaluationResponse from(PeerEvaluation e) {
        return new PeerEvaluationResponse(
                e.getId(),
                e.getEvaluator().getId(),
                e.getEvaluator().getFirstName() + " " + e.getEvaluator().getLastName(),
                e.getEvaluatee().getId(),
                e.getEvaluatee().getFirstName() + " " + e.getEvaluatee().getLastName(),
                e.getWeek().getId(),
                e.getPublicComments(),
                e.getPrivateComments(),
                e.isSubmitted(),
                e.getSubmittedAt(),
                e.getScores().stream().map(ScoreResponse::from).toList()
        );
    }
}
