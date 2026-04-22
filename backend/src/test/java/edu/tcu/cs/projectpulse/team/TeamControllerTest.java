package edu.tcu.cs.projectpulse.team;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tcu.cs.projectpulse.common.exception.GlobalExceptionHandler;
import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.team.dto.AssignMembersRequest;
import edu.tcu.cs.projectpulse.team.dto.TeamRequest;
import edu.tcu.cs.projectpulse.team.dto.TeamResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TeamControllerTest {

    @Mock TeamService teamService;
    @InjectMocks TeamController teamController;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private TeamResponse teamResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(teamController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        teamResponse = new TeamResponse(1L, "Team Alpha", "Senior Design team",
                null, 1L, "2024-2025", List.of(), List.of());
    }

    // ── GET /api/teams ────────────────────────────────────────────────────────

    @Test
    void findAll_returns200WithList() throws Exception {
        given(teamService.findAll(null, null)).willReturn(List.of(teamResponse));

        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Team Alpha"));
    }

    @Test
    void findAll_givenSectionIdParam_delegatesToService() throws Exception {
        given(teamService.findAll(1L, null)).willReturn(List.of(teamResponse));

        mockMvc.perform(get("/api/teams?sectionId=1"))
                .andExpect(status().isOk());

        then(teamService).should().findAll(1L, null);
    }

    // ── GET /api/teams/{id} ───────────────────────────────────────────────────

    @Test
    void findById_givenValidId_returns200() throws Exception {
        given(teamService.findById(1L)).willReturn(teamResponse);

        mockMvc.perform(get("/api/teams/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Team Alpha"));
    }

    @Test
    void findById_givenInvalidId_returns404() throws Exception {
        given(teamService.findById(99L)).willThrow(new ObjectNotFoundException("Team", 99L));

        mockMvc.perform(get("/api/teams/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/teams ───────────────────────────────────────────────────────

    @Test
    void create_givenValidRequest_returns201() throws Exception {
        TeamRequest req = new TeamRequest("Team Alpha", "Senior Design team", null, 1L);
        given(teamService.create(any(TeamRequest.class))).willReturn(teamResponse);

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Team Alpha"));
    }

    @Test
    void create_givenDuplicateName_returns409() throws Exception {
        TeamRequest req = new TeamRequest("Team Alpha", null, null, 1L);
        given(teamService.create(any())).willThrow(
                new IllegalStateException("Team \"Team Alpha\" already exists."));

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    // ── PUT /api/teams/{id} ───────────────────────────────────────────────────

    @Test
    void update_givenValidRequest_returns200() throws Exception {
        TeamRequest req = new TeamRequest("Team Alpha Renamed", null, null, 1L);
        TeamResponse updated = new TeamResponse(1L, "Team Alpha Renamed", null,
                null, 1L, "2024-2025", List.of(), List.of());
        given(teamService.update(eq(1L), any(TeamRequest.class))).willReturn(updated);

        mockMvc.perform(put("/api/teams/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Team Alpha Renamed"));
    }

    // ── DELETE /api/teams/{id} ────────────────────────────────────────────────

    @Test
    void delete_givenValidId_returns204() throws Exception {
        willDoNothing().given(teamService).delete(1L);

        mockMvc.perform(delete("/api/teams/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_givenInvalidId_returns404() throws Exception {
        willThrow(new ObjectNotFoundException("Team", 99L)).given(teamService).delete(99L);

        mockMvc.perform(delete("/api/teams/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/teams/{id}/students ─────────────────────────────────────────

    @Test
    void assignStudents_givenValidRequest_returns200() throws Exception {
        AssignMembersRequest req = new AssignMembersRequest(List.of(10L));
        given(teamService.assignStudents(eq(1L), any(AssignMembersRequest.class))).willReturn(teamResponse);

        mockMvc.perform(post("/api/teams/1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ── DELETE /api/teams/{teamId}/students/{studentId} ───────────────────────

    @Test
    void removeStudent_givenValidIds_returns204() throws Exception {
        willDoNothing().given(teamService).removeStudent(1L, 10L);

        mockMvc.perform(delete("/api/teams/1/students/10"))
                .andExpect(status().isNoContent());
    }
}
