package edu.tcu.cs.projectpulse.report;

import edu.tcu.cs.projectpulse.common.exception.GlobalExceptionHandler;
import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.report.dto.SectionPeerReportResponse;
import edu.tcu.cs.projectpulse.report.dto.StudentPeerRangeResponse;
import edu.tcu.cs.projectpulse.report.dto.StudentWARRangeResponse;
import edu.tcu.cs.projectpulse.report.dto.TeamWARReportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock ReportService reportService;
    @InjectMocks ReportController reportController;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── GET /api/sections/{id}/peer-evaluation-report ─────────────────────────

    @Test
    void sectionPeerReport_returns200() throws Exception {
        SectionPeerReportResponse resp = new SectionPeerReportResponse(
                1L, "2024-2025", 10L, "2024-10-07", List.of(), List.of()
        );
        given(reportService.sectionPeerReport(1L, 10L)).willReturn(resp);

        mockMvc.perform(get("/api/sections/1/peer-evaluation-report?weekId=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sectionName").value("2024-2025"));
    }

    @Test
    void sectionPeerReport_givenInvalidSection_returns404() throws Exception {
        given(reportService.sectionPeerReport(99L, 10L))
                .willThrow(new ObjectNotFoundException("Section", 99L));

        mockMvc.perform(get("/api/sections/99/peer-evaluation-report?weekId=10"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/teams/{id}/war-report ────────────────────────────────────────

    @Test
    void teamWARReport_returns200() throws Exception {
        TeamWARReportResponse resp = new TeamWARReportResponse(
                1L, "Team Alpha", 10L, "2024-10-07", List.of(), List.of()
        );
        given(reportService.teamWARReport(1L, 10L)).willReturn(resp);

        mockMvc.perform(get("/api/teams/1/war-report?weekId=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value("Team Alpha"));
    }

    // ── GET /api/students/{id}/peer-evaluation-report ─────────────────────────

    @Test
    void studentPeerReport_returns200() throws Exception {
        StudentPeerRangeResponse resp = new StudentPeerRangeResponse(
                1L, "Alice Smith", List.of()
        );
        given(reportService.studentPeerReport(eq(1L), any(), any())).willReturn(resp);

        mockMvc.perform(get("/api/students/1/peer-evaluation-report?start=2024-08-01&end=2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentName").value("Alice Smith"));
    }

    // ── GET /api/students/{id}/war-report ─────────────────────────────────────

    @Test
    void studentWARReport_returns200() throws Exception {
        StudentWARRangeResponse resp = new StudentWARRangeResponse(
                1L, "Alice Smith", List.of()
        );
        given(reportService.studentWARReport(eq(1L), any(), any())).willReturn(resp);

        mockMvc.perform(get("/api/students/1/war-report?start=2024-08-01&end=2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentName").value("Alice Smith"));
    }

    @Test
    void studentWARReport_givenInvalidStudent_returns404() throws Exception {
        given(reportService.studentWARReport(eq(99L), any(), any()))
                .willThrow(new ObjectNotFoundException("User", 99L));

        mockMvc.perform(get("/api/students/99/war-report?start=2024-08-01&end=2024-12-31"))
                .andExpect(status().isNotFound());
    }
}
