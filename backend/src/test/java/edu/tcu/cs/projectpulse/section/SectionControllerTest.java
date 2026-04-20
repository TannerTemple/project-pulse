package edu.tcu.cs.projectpulse.section;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tcu.cs.projectpulse.common.exception.GlobalExceptionHandler;
import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.section.dto.SectionRequest;
import edu.tcu.cs.projectpulse.section.dto.SectionResponse;
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
class SectionControllerTest {

    @Mock SectionService sectionService;
    @InjectMocks SectionController sectionController;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sectionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── GET /api/sections ─────────────────────────────────────────────────────

    @Test
    void findAll_returnsOkWithEmptyList() throws Exception {
        given(sectionService.findAll(null)).willReturn(List.of());

        mockMvc.perform(get("/api/sections"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void findAll_givenNameParam_delegatesToService() throws Exception {
        given(sectionService.findAll("2024")).willReturn(List.of());

        mockMvc.perform(get("/api/sections?name=2024"))
                .andExpect(status().isOk());

        then(sectionService).should().findAll("2024");
    }

    // ── GET /api/sections/{id} ────────────────────────────────────────────────

    @Test
    void findById_givenValidId_returns200WithSection() throws Exception {
        SectionResponse resp = new SectionResponse(1L, "2024-2025",
                LocalDate.of(2024, 8, 26), LocalDate.of(2025, 5, 10), null, List.of());
        given(sectionService.findById(1L)).willReturn(resp);

        mockMvc.perform(get("/api/sections/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("2024-2025"));
    }

    @Test
    void findById_givenInvalidId_returns404() throws Exception {
        given(sectionService.findById(99L)).willThrow(new ObjectNotFoundException("Section", 99L));

        mockMvc.perform(get("/api/sections/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/sections ────────────────────────────────────────────────────

    @Test
    void create_givenValidRequest_returns201() throws Exception {
        SectionResponse resp = new SectionResponse(2L, "2025-2026",
                LocalDate.of(2025, 8, 25), LocalDate.of(2026, 5, 9), null, List.of());
        given(sectionService.create(any(SectionRequest.class))).willReturn(resp);

        mockMvc.perform(post("/api/sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"2025-2026\",\"startDate\":\"2025-08-25\",\"endDate\":\"2026-05-09\",\"rubricId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("2025-2026"));
    }

    @Test
    void create_givenDuplicateName_returns409() throws Exception {
        given(sectionService.create(any())).willThrow(
                new IllegalStateException("Section \"2024-2025\" already exists."));

        mockMvc.perform(post("/api/sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"2024-2025\",\"startDate\":\"2024-08-26\",\"endDate\":\"2025-05-10\",\"rubricId\":1}"))
                .andExpect(status().isConflict());
    }
}
