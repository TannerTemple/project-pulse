package edu.tcu.cs.projectpulse.team;

import edu.tcu.cs.projectpulse.team.dto.AssignMembersRequest;
import edu.tcu.cs.projectpulse.team.dto.TeamRequest;
import edu.tcu.cs.projectpulse.team.dto.TeamResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public List<TeamResponse> findAll(
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String name) {
        return teamService.findAll(sectionId, name);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR','STUDENT')")
    public TeamResponse findById(@PathVariable Long id) {
        return teamService.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TeamResponse update(@PathVariable Long id, @Valid @RequestBody TeamRequest request) {
        return teamService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Member assignment endpoints ───────────────────────────────────────────

    @PostMapping("/{id}/students")
    @PreAuthorize("hasRole('ADMIN')")
    public TeamResponse assignStudents(@PathVariable Long id,
                                       @Valid @RequestBody AssignMembersRequest request) {
        return teamService.assignStudents(id, request);
    }

    @DeleteMapping("/{teamId}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeStudent(@PathVariable Long teamId,
                                              @PathVariable Long studentId) {
        teamService.removeStudent(teamId, studentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/instructors")
    @PreAuthorize("hasRole('ADMIN')")
    public TeamResponse assignInstructors(@PathVariable Long id,
                                          @Valid @RequestBody AssignMembersRequest request) {
        return teamService.assignInstructors(id, request);
    }

    @DeleteMapping("/{teamId}/instructors/{instructorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeInstructor(@PathVariable Long teamId,
                                                 @PathVariable Long instructorId) {
        teamService.removeInstructor(teamId, instructorId);
        return ResponseEntity.noContent().build();
    }
}
