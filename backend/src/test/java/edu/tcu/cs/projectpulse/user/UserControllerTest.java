package edu.tcu.cs.projectpulse.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tcu.cs.projectpulse.common.exception.GlobalExceptionHandler;
import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.user.dto.InviteRequest;
import edu.tcu.cs.projectpulse.user.dto.UserResponse;
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
class UserControllerTest {

    @Mock UserService userService;
    @InjectMocks UserController userController;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private UserResponse studentResponse;
    private UserResponse instructorResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        studentResponse = new UserResponse(1L, "Alice", "Smith", null,
                "alice@tcu.edu", UserRole.STUDENT, true, true, 1L, "2024-2025", 1L, "Team Alpha");

        instructorResponse = new UserResponse(2L, "Bob", "Jones", null,
                "bob@tcu.edu", UserRole.INSTRUCTOR, true, true, null, null, null, null);
    }

    // ── GET /api/users/me ─────────────────────────────────────────────────────

    @Test
    void getMe_returns200() throws Exception {
        given(userService.getMe()).willReturn(studentResponse);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@tcu.edu"));
    }

    // ── GET /api/students ─────────────────────────────────────────────────────

    @Test
    void findStudents_returns200WithList() throws Exception {
        given(userService.findStudents(null, null, null, null, null))
                .willReturn(List.of(studentResponse));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("alice@tcu.edu"));
    }

    @Test
    void findStudents_givenLastNameFilter_delegatesToService() throws Exception {
        given(userService.findStudents(null, "Smith", null, null, null))
                .willReturn(List.of(studentResponse));

        mockMvc.perform(get("/api/students?lastName=Smith"))
                .andExpect(status().isOk());

        then(userService).should().findStudents(null, "Smith", null, null, null);
    }

    // ── GET /api/students/{id} ────────────────────────────────────────────────

    @Test
    void findStudent_givenValidId_returns200() throws Exception {
        given(userService.findById(1L)).willReturn(studentResponse);

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    void findStudent_givenInvalidId_returns404() throws Exception {
        given(userService.findById(99L)).willThrow(new ObjectNotFoundException("User", 99L));

        mockMvc.perform(get("/api/students/99"))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/students/{id} ─────────────────────────────────────────────

    @Test
    void deleteStudent_givenValidId_returns204() throws Exception {
        willDoNothing().given(userService).deleteStudent(1L);

        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteStudent_givenNonStudent_returns400() throws Exception {
        willThrow(new IllegalArgumentException("User 2 is not a student."))
                .given(userService).deleteStudent(2L);

        mockMvc.perform(delete("/api/students/2"))
                .andExpect(status().isBadRequest());
    }

    // ── POST /api/sections/{sectionId}/invitations/students ───────────────────

    @Test
    void inviteStudents_returns200WithCount() throws Exception {
        InviteRequest req = new InviteRequest("alice@tcu.edu; bob@tcu.edu", null);
        given(userService.inviteStudents(eq(1L), any(InviteRequest.class))).willReturn(2);

        mockMvc.perform(post("/api/sections/1/invitations/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailsSent").value(2));
    }

    // ── GET /api/instructors ──────────────────────────────────────────────────

    @Test
    void findInstructors_returns200WithList() throws Exception {
        given(userService.findInstructors(null, null, null, null))
                .willReturn(List.of(instructorResponse));

        mockMvc.perform(get("/api/instructors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("bob@tcu.edu"));
    }

    // ── GET /api/instructors/{id} ─────────────────────────────────────────────

    @Test
    void findInstructor_givenValidId_returns200() throws Exception {
        given(userService.findById(2L)).willReturn(instructorResponse);

        mockMvc.perform(get("/api/instructors/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("bob@tcu.edu"));
    }

    // ── PATCH /api/instructors/{id}/deactivate ────────────────────────────────

    @Test
    void deactivate_givenValidId_returns200() throws Exception {
        UserResponse deactivated = new UserResponse(2L, "Bob", "Jones", null,
                "bob@tcu.edu", UserRole.INSTRUCTOR, false, true, null, null, null, null);
        given(userService.deactivate(2L)).willReturn(deactivated);

        mockMvc.perform(patch("/api/instructors/2/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void deactivate_givenStudent_returns400() throws Exception {
        given(userService.deactivate(1L)).willThrow(
                new IllegalArgumentException("Only instructors can be deactivated."));

        mockMvc.perform(patch("/api/instructors/1/deactivate"))
                .andExpect(status().isBadRequest());
    }

    // ── PATCH /api/instructors/{id}/reactivate ────────────────────────────────

    @Test
    void reactivate_givenValidId_returns200() throws Exception {
        given(userService.reactivate(2L)).willReturn(instructorResponse);

        mockMvc.perform(patch("/api/instructors/2/reactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    // ── POST /api/invitations/instructors ─────────────────────────────────────

    @Test
    void inviteInstructors_returns200WithCount() throws Exception {
        InviteRequest req = new InviteRequest("instructor@tcu.edu", null);
        given(userService.inviteInstructors(any(InviteRequest.class))).willReturn(1);

        mockMvc.perform(post("/api/invitations/instructors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailsSent").value(1));
    }
}
