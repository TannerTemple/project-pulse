package edu.tcu.cs.projectpulse.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TeamRequest(
        @NotBlank String name,
        String description,
        String websiteUrl,
        @NotNull Long sectionId
) {}
