package edu.tcu.cs.projectpulse.war.dto;

import edu.tcu.cs.projectpulse.war.ActivityCategory;
import edu.tcu.cs.projectpulse.war.ActivityStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WARActivityRequest(
        @NotNull ActivityCategory category,
        @NotBlank String activity,
        String description,
        @Min(0) double plannedHours,
        Double actualHours,
        @NotNull ActivityStatus status,
        @NotNull Long weekId
) {}
