package edu.tcu.cs.projectpulse.team;

import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.email.EmailService;
import edu.tcu.cs.projectpulse.section.SectionService;
import edu.tcu.cs.projectpulse.team.dto.AssignMembersRequest;
import edu.tcu.cs.projectpulse.team.dto.TeamRequest;
import edu.tcu.cs.projectpulse.team.dto.TeamResponse;
import edu.tcu.cs.projectpulse.user.AppUser;
import edu.tcu.cs.projectpulse.user.UserRepository;
import edu.tcu.cs.projectpulse.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final SectionService sectionService;
    private final EmailService emailService;

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Transactional(readOnly = true)
    public List<TeamResponse> findAll(Long sectionId, String name) {
        List<Team> teams;
        if (sectionId != null) {
            teams = teamRepository.findBySectionId(sectionId);
        } else if (name != null && !name.isBlank()) {
            teams = teamRepository.findByNameContainingIgnoreCase(name);
        } else {
            teams = teamRepository.findAll();
        }
        return teams.stream()
                .sorted((a, b) -> {
                    int sectionCmp = b.getSection().getName().compareTo(a.getSection().getName());
                    return sectionCmp != 0 ? sectionCmp : a.getName().compareTo(b.getName());
                })
                .map(TeamResponse::from)
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Transactional(readOnly = true)
    public TeamResponse findById(Long id) {
        return TeamResponse.from(getTeamOrThrow(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TeamResponse create(TeamRequest request) {
        if (teamRepository.existsByName(request.name())) {
            throw new IllegalStateException("Team \"" + request.name() + "\" already exists.");
        }
        Team team = new Team();
        team.setName(request.name());
        team.setDescription(request.description());
        team.setWebsiteUrl(request.websiteUrl());
        team.setSection(sectionService.getSectionOrThrow(request.sectionId()));
        return TeamResponse.from(teamRepository.save(team));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TeamResponse update(Long id, TeamRequest request) {
        Team team = getTeamOrThrow(id);
        if (!team.getName().equals(request.name()) && teamRepository.existsByName(request.name())) {
            throw new IllegalStateException("Team \"" + request.name() + "\" already exists.");
        }
        team.setName(request.name());
        team.setDescription(request.description());
        team.setWebsiteUrl(request.websiteUrl());
        return TeamResponse.from(teamRepository.save(team));
    }

    /** Assigns students to a team and notifies them (UC-12). */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TeamResponse assignStudents(Long teamId, AssignMembersRequest request) {
        Team team = getTeamOrThrow(teamId);
        for (Long userId : request.userIds()) {
            AppUser user = getUserOrThrow(userId);
            if (user.getRole() != UserRole.STUDENT) {
                throw new IllegalArgumentException("User " + userId + " is not a student.");
            }
            user.setTeam(team);
            userRepository.save(user);
            emailService.sendTeamAssignment(user.getEmail(), user.getFirstName(), team.getName());
        }
        return TeamResponse.from(teamRepository.findById(teamId).orElseThrow());
    }

    /** Removes a student from a team (UC-13). */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void removeStudent(Long teamId, Long studentId) {
        getTeamOrThrow(teamId);
        AppUser student = getUserOrThrow(studentId);
        if (student.getTeam() == null || !student.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("Student is not a member of this team.");
        }
        student.setTeam(null);
        userRepository.save(student);
        emailService.sendTeamRemoval(student.getEmail(), student.getFirstName(),
                student.getTeam() != null ? student.getTeam().getName() : "your team");
    }

    /** Assigns instructors to a team (UC-19). */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TeamResponse assignInstructors(Long teamId, AssignMembersRequest request) {
        Team team = getTeamOrThrow(teamId);
        for (Long userId : request.userIds()) {
            AppUser instructor = getUserOrThrow(userId);
            if (instructor.getRole() != UserRole.INSTRUCTOR) {
                throw new IllegalArgumentException("User " + userId + " is not an instructor.");
            }
            if (!team.getInstructors().contains(instructor)) {
                team.getInstructors().add(instructor);
                emailService.sendTeamAssignment(
                        instructor.getEmail(), instructor.getFirstName(), team.getName());
            }
        }
        return TeamResponse.from(teamRepository.save(team));
    }

    /** Removes an instructor from a team (UC-20). */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void removeInstructor(Long teamId, Long instructorId) {
        Team team = getTeamOrThrow(teamId);
        AppUser instructor = getUserOrThrow(instructorId);
        boolean removed = team.getInstructors().remove(instructor);
        if (!removed) {
            throw new IllegalArgumentException("Instructor is not assigned to this team.");
        }
        teamRepository.save(team);
        emailService.sendTeamRemoval(instructor.getEmail(), instructor.getFirstName(), team.getName());
    }

    /** Permanently deletes a team and cascades to WARs/evals via the service layer (UC-14). */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        Team team = getTeamOrThrow(id);

        // Notify and detach members before deleting
        team.getStudents().forEach(s -> {
            emailService.sendTeamRemoval(s.getEmail(), s.getFirstName(), team.getName());
            s.setTeam(null);
            userRepository.save(s);
        });
        team.getInstructors().forEach(i ->
                emailService.sendTeamRemoval(i.getEmail(), i.getFirstName(), team.getName()));
        team.getInstructors().clear();

        teamRepository.delete(team);
    }

    // ── private ───────────────────────────────────────────────────────────────

    public Team getTeamOrThrow(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Team", id));
    }

    private AppUser getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("User", id));
    }
}
