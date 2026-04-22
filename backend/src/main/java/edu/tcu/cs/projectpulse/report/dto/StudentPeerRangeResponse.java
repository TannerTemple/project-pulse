package edu.tcu.cs.projectpulse.report.dto;

import java.math.BigDecimal;
import java.util.List;

/** Per-student peer eval report over a date range (UC-33). Instructor view includes private data. */
public record StudentPeerRangeResponse(
        Long studentId,
        String studentName,
        List<WeekEntry> weeks
) {
    public record WeekEntry(
            Long weekId,
            String weekStart,
            BigDecimal grade,
            List<StudentPeerSummary.EvaluatorDetail> evaluations
    ) {}
}
