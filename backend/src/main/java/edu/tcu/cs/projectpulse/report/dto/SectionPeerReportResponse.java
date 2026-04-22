package edu.tcu.cs.projectpulse.report.dto;

import java.util.List;

/** Section-wide peer evaluation report for one week (UC-31). */
public record SectionPeerReportResponse(
        Long sectionId,
        String sectionName,
        Long weekId,
        String weekStart,
        List<StudentPeerSummary> students,
        List<String> nonSubmitters
) {}
