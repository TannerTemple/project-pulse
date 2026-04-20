package edu.tcu.cs.projectpulse.peerevaluation.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Student's own peer evaluation report for one week (UC-29).
 * Evaluator identities and private comments are never included (BR-5).
 */
public record PeerEvalReportResponse(
        Long weekId,
        Long studentId,
        String studentName,
        int evaluatorCount,
        BigDecimal overallGrade,
        List<CriterionReportDto> criterionAverages,
        List<String> publicComments
) {}
