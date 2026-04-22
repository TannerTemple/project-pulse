package edu.tcu.cs.projectpulse.report.dto;

import edu.tcu.cs.projectpulse.war.ActivityCategory;
import edu.tcu.cs.projectpulse.war.ActivityStatus;

import java.util.List;

/** Team WAR report for one week (UC-32). */
public record TeamWARReportResponse(
        Long teamId,
        String teamName,
        Long weekId,
        String weekStart,
        List<StudentWAREntry> entries,
        List<String> nonSubmitters
) {
    public record StudentWAREntry(
            Long studentId,
            String studentName,
            List<ActivityRow> activities
    ) {}

    public record ActivityRow(
            Long id,
            ActivityCategory category,
            String activity,
            String description,
            double plannedHours,
            Double actualHours,
            ActivityStatus status
    ) {}
}
