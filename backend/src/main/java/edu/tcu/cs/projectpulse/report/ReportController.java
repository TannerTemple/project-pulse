package edu.tcu.cs.projectpulse.report;

import edu.tcu.cs.projectpulse.report.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /** GET /api/sections/{id}/peer-evaluation-report?weekId= — UC-31 */
    @GetMapping("/api/sections/{id}/peer-evaluation-report")
    public SectionPeerReportResponse sectionPeerReport(
            @PathVariable Long id,
            @RequestParam Long weekId) {
        return reportService.sectionPeerReport(id, weekId);
    }

    /** GET /api/teams/{id}/war-report?weekId= — UC-32 */
    @GetMapping("/api/teams/{id}/war-report")
    public TeamWARReportResponse teamWARReport(
            @PathVariable Long id,
            @RequestParam Long weekId) {
        return reportService.teamWARReport(id, weekId);
    }

    /** GET /api/students/{id}/peer-evaluation-report?start=&end= — UC-33 */
    @GetMapping("/api/students/{id}/peer-evaluation-report")
    public StudentPeerRangeResponse studentPeerReport(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return reportService.studentPeerReport(id, start, end);
    }

    /** GET /api/students/{id}/war-report?start=&end= — UC-34 */
    @GetMapping("/api/students/{id}/war-report")
    public StudentWARRangeResponse studentWARReport(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return reportService.studentWARReport(id, start, end);
    }
}
