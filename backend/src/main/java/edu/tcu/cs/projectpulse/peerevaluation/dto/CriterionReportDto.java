package edu.tcu.cs.projectpulse.peerevaluation.dto;

import java.math.BigDecimal;

/** A single criterion's averaged score in the student's own report (UC-29, BR-5). */
public record CriterionReportDto(
        Long criterionId,
        String criterionName,
        BigDecimal averageScore,
        BigDecimal maxScore
) {}
