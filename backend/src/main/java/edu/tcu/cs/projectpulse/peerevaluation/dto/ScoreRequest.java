package edu.tcu.cs.projectpulse.peerevaluation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ScoreRequest(
        @NotNull Long criterionId,
        @NotNull @Min(0) Integer score
) {}
