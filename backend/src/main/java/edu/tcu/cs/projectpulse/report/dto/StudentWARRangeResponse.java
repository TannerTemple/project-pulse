package edu.tcu.cs.projectpulse.report.dto;

import java.util.List;

/** Per-student WAR report over a date range (UC-34). */
public record StudentWARRangeResponse(
        Long studentId,
        String studentName,
        List<WeekEntry> weeks
) {
    public record WeekEntry(
            Long weekId,
            String weekStart,
            List<TeamWARReportResponse.ActivityRow> activities
    ) {}
}
