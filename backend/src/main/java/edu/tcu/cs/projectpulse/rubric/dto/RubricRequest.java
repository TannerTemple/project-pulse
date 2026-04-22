package edu.tcu.cs.projectpulse.rubric.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RubricRequest(
        @NotBlank String name,
        @NotEmpty @Valid List<CriterionDto> criteria
) {}
