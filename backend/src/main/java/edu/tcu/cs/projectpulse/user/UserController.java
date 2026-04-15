package edu.tcu.cs.projectpulse.user;

import edu.tcu.cs.projectpulse.user.dto.InviteRequest;
import edu.tcu.cs.projectpulse.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── Students ──────────────────────────────────────────────────────────────

    @GetMapping("/api/students")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public List<UserResponse> findStudents(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long teamId) {
        return userService.findStudents(firstName, lastName, email, sectionId, teamId);
    }

    @GetMapping("/api/students/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public UserResponse findStudent(@PathVariable Long id) {
        return userService.findById(id);
    }

    @DeleteMapping("/api/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        userService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    /** POST /api/sections/{sectionId}/invitations/students — UC-11 */
    @PostMapping("/api/sections/{sectionId}/invitations/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> inviteStudents(
            @PathVariable Long sectionId,
            @Valid @RequestBody InviteRequest request) {
        int count = userService.inviteStudents(sectionId, request);
        return ResponseEntity.ok(Map.of("emailsSent", count));
    }

    // ── Instructors ───────────────────────────────────────────────────────────

    @GetMapping("/api/instructors")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> findInstructors(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) Boolean active) {
        return userService.findInstructors(firstName, lastName, teamName, active);
    }

    @GetMapping("/api/instructors/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse findInstructor(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PatchMapping("/api/instructors/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse deactivate(@PathVariable Long id) {
        return userService.deactivate(id);
    }

    @PatchMapping("/api/instructors/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse reactivate(@PathVariable Long id) {
        return userService.reactivate(id);
    }

    /** POST /api/invitations/instructors — UC-18 */
    @PostMapping("/api/invitations/instructors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> inviteInstructors(
            @Valid @RequestBody InviteRequest request) {
        int count = userService.inviteInstructors(request);
        return ResponseEntity.ok(Map.of("emailsSent", count));
    }
}
