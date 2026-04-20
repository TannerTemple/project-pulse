package edu.tcu.cs.projectpulse.war;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tcu.cs.projectpulse.common.exception.GlobalExceptionHandler;
import edu.tcu.cs.projectpulse.war.dto.WARActivityRequest;
import edu.tcu.cs.projectpulse.war.dto.WARActivityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class WARActivityControllerTest {

    @Mock WARActivityService warService;
    @InjectMocks WARActivityController warController;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(warController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── GET /api/war-activities ───────────────────────────────────────────────

    @Test
    void findMyActivities_returns200WithList() throws Exception {
        given(warService.findMyActivities(1L)).willReturn(List.of());

        mockMvc.perform(get("/api/war-activities?weekId=1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // ── POST /api/war-activities ──────────────────────────────────────────────

    @Test
    void create_givenValidRequest_returns201() throws Exception {
        WARActivityRequest req = new WARActivityRequest(
                ActivityCategory.DEVELOPMENT, "Write tests", "Unit tests for WAR",
                3.0, null, ActivityStatus.IN_PROGRESS, 1L
        );
        WARActivityResponse resp = new WARActivityResponse(
                99L, ActivityCategory.DEVELOPMENT, "Write tests", "Unit tests for WAR",
                3.0, null, ActivityStatus.IN_PROGRESS,
                1L, LocalDate.now().minusWeeks(1), 1L, "Alice Smith"
        );
        given(warService.create(any())).willReturn(resp);

        mockMvc.perform(post("/api/war-activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activity").value("Write tests"))
                .andExpect(jsonPath("$.category").value("DEVELOPMENT"));
    }

    @Test
    void create_givenFutureWeek_returns400() throws Exception {
        WARActivityRequest req = new WARActivityRequest(
                ActivityCategory.TESTING, "Future task", null,
                2.0, null, ActivityStatus.IN_PROGRESS, 99L
        );
        given(warService.create(any())).willThrow(
                new IllegalArgumentException("Cannot submit WAR activities for a future week."));

        mockMvc.perform(post("/api/war-activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/war-activities/{id} ──────────────────────────────────────────

    @Test
    void update_givenValidRequest_returns200() throws Exception {
        WARActivityRequest req = new WARActivityRequest(
                ActivityCategory.DEVELOPMENT, "Updated task", null,
                4.0, 3.5, ActivityStatus.DONE, 1L
        );
        WARActivityResponse resp = new WARActivityResponse(
                5L, ActivityCategory.DEVELOPMENT, "Updated task", null,
                4.0, 3.5, ActivityStatus.DONE,
                1L, LocalDate.now().minusWeeks(1), 1L, "Alice Smith"
        );
        given(warService.update(eq(5L), any())).willReturn(resp);

        mockMvc.perform(put("/api/war-activities/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    // ── DELETE /api/war-activities/{id} ───────────────────────────────────────

    @Test
    void delete_returns204() throws Exception {
        willDoNothing().given(warService).delete(5L);

        mockMvc.perform(delete("/api/war-activities/5"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_givenOtherStudentsActivity_returns400() throws Exception {
        willThrow(new IllegalArgumentException("You can only delete your own WAR activities."))
                .given(warService).delete(5L);

        mockMvc.perform(delete("/api/war-activities/5"))
                .andExpect(status().isBadRequest());
    }
}
