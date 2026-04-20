package edu.tcu.cs.projectpulse.peerevaluation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PeerEvaluationRequest(
        @NotNull Long evaluateeId,
        @NotNull Long weekId,
        String publicComments,
        String privateComments,
        @NotEmpty @Valid List<ScoreRequest> scores
) {}
