package edu.tcu.cs.projectpulse.rubric;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tcu.cs.projectpulse.common.exception.GlobalExceptionHandler;
import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.rubric.dto.CriterionDto;
import edu.tcu.cs.projectpulse.rubric.dto.RubricRequest;
import edu.tcu.cs.projectpulse.rubric.dto.RubricResponse;
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
class RubricControllerTest {

    @Mock RubricService rubricService;
    @InjectMocks RubricController rubricController;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rubricController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── GET /api/rubrics ──────────────────────────────────────────────────────

    @Test
    void findAll_returns200WithList() throws Exception {
        RubricResponse resp = new RubricResponse(1L, "Peer Eval v1", List.of());
        given(rubricService.findAll()).willReturn(List.of(resp));

        mockMvc.perform(get("/api/rubrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Peer Eval v1"));
    }

    @Test
    void findAll_givenNoRubrics_returns200WithEmptyList() throws Exception {
        given(rubricService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/rubrics"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // ── GET /api/rubrics/{id} ─────────────────────────────────────────────────

    @Test
    void findById_givenValidId_returns200() throws Exception {
        RubricResponse resp = new RubricResponse(1L, "Peer Eval v1", List.of());
        given(rubricService.findById(1L)).willReturn(resp);

        mockMvc.perform(get("/api/rubrics/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Peer Eval v1"));
    }

    @Test
    void findById_givenInvalidId_returns404() throws Exception {
        given(rubricService.findById(99L)).willThrow(new ObjectNotFoundException("Rubric", 99L));

        mockMvc.perform(get("/api/rubrics/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/rubrics ─────────────────────────────────────────────────────

    @Test
    void create_givenValidRequest_returns201() throws Exception {
        RubricRequest req = new RubricRequest("Peer Eval v1", List.of(
                new CriterionDto(null, "Quality", "Code quality", new BigDecimal("10"), 0)
        ));
        RubricResponse resp = new RubricResponse(1L, "Peer Eval v1", List.of());
        given(rubricService.create(any(RubricRequest.class))).willReturn(resp);

        mockMvc.perform(post("/api/rubrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Peer Eval v1"));
    }

    @Test
    void create_givenDuplicateName_returns409() throws Exception {
        RubricRequest req = new RubricRequest("Peer Eval v1", List.of(
                new CriterionDto(null, "Quality", null, new BigDecimal("10"), 0)
        ));
        given(rubricService.create(any())).willThrow(
                new IllegalStateException("Rubric \"Peer Eval v1\" already exists."));

        mockMvc.perform(post("/api/rubrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }
}
