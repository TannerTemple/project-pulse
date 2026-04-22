package edu.tcu.cs.projectpulse.peerevaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tcu.cs.projectpulse.common.exception.GlobalExceptionHandler;
import edu.tcu.cs.projectpulse.peerevaluation.dto.PeerEvalReportResponse;
import edu.tcu.cs.projectpulse.peerevaluation.dto.PeerEvaluationRequest;
import edu.tcu.cs.projectpulse.peerevaluation.dto.PeerEvaluationResponse;
import edu.tcu.cs.projectpulse.peerevaluation.dto.ScoreRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PeerEvaluationControllerTest {

    @Mock PeerEvaluationService evalService;
    @InjectMocks PeerEvaluationController evalController;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(evalController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── POST /api/peer-evaluations ────────────────────────────────────────────

    @Test
    void submit_givenValidRequest_returns201() throws Exception {
        PeerEvaluationRequest req = new PeerEvaluationRequest(
                2L, 10L, "Great work!", null,
                List.of(new ScoreRequest(1L, 9))
        );
        PeerEvaluationResponse resp = new PeerEvaluationResponse(
                99L, 1L, "Alice Smith", 2L, "Bob Jones",
                10L, "Great work!", null, true, null, List.of()
        );
        given(evalService.submit(any())).willReturn(resp);

        mockMvc.perform(post("/api/peer-evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.submitted").value(true))
                .andExpect(jsonPath("$.evaluateeName").value("Bob Jones"));
    }

    @Test
    void submit_givenAlreadySubmitted_returns409() throws Exception {
        PeerEvaluationRequest req = new PeerEvaluationRequest(
                2L, 10L, null, null, List.of(new ScoreRequest(1L, 8))
        );
        given(evalService.submit(any()))
                .willThrow(new IllegalStateException("Evaluation already submitted."));

        mockMvc.perform(post("/api/peer-evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void submit_givenInactiveWeek_returns400() throws Exception {
        PeerEvaluationRequest req = new PeerEvaluationRequest(
                2L, 10L, null, null, List.of(new ScoreRequest(1L, 7))
        );
        given(evalService.submit(any()))
                .willThrow(new IllegalArgumentException("Week is not active."));

        mockMvc.perform(post("/api/peer-evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/peer-evaluations/me/report ───────────────────────────────────

    @Test
    void getMyReport_returns200WithReport() throws Exception {
        PeerEvalReportResponse report = new PeerEvalReportResponse(
                10L, 1L, "Alice Smith", 2,
                new BigDecimal("54.00"), List.of(), List.of("Good job")
        );
        given(evalService.getMyReport(10L)).willReturn(report);

        mockMvc.perform(get("/api/peer-evaluations/me/report?weekId=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentName").value("Alice Smith"))
                .andExpect(jsonPath("$.evaluatorCount").value(2));
    }
}
