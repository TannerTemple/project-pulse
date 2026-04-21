package edu.tcu.cs.projectpulse.team;

import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.email.EmailService;
import edu.tcu.cs.projectpulse.section.Section;
import edu.tcu.cs.projectpulse.section.SectionService;
import edu.tcu.cs.projectpulse.team.dto.AssignMembersRequest;
import edu.tcu.cs.projectpulse.team.dto.TeamRequest;
import edu.tcu.cs.projectpulse.team.dto.TeamResponse;
import edu.tcu.cs.projectpulse.user.AppUser;
import edu.tcu.cs.projectpulse.user.UserRepository;
import edu.tcu.cs.projectpulse.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock TeamRepository teamRepository;
    @Mock UserRepository userRepository;
    @Mock SectionService sectionService;
    @Mock EmailService emailService;
    @InjectMocks TeamService teamService;

    private Section section;
    private Team team;
    private AppUser student;

    @BeforeEach
    void setUp() {
        section = new Section();
        section.setId(1L);
        section.setName("2024-2025");

        team = new Team();
        team.setId(1L);
        team.setName("Team Alpha");
        team.setSection(section);
        team.setStudents(new ArrayList<>());
        team.setInstructors(new ArrayList<>());

        student = new AppUser();
        student.setId(10L);
        student.setFirstName("Alice");
        student.setLastName("Smith");
        student.setEmail("alice@tcu.edu");
        student.setRole(UserRole.STUDENT);
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsAllTeams() {
        given(teamRepository.findAll()).willReturn(List.of(team));

        List<TeamResponse> result = teamService.findAll(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Team Alpha");
    }

    @Test
    void findAll_givenSectionId_filtersToSection() {
        given(teamRepository.findBySectionId(1L)).willReturn(List.of(team));

        List<TeamResponse> result = teamService.findAll(1L, null);

        assertThat(result).hasSize(1);
        then(teamRepository).should().findBySectionId(1L);
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_givenValidRequest_savesTeam() {
        given(teamRepository.existsByName("Team Beta")).willReturn(false);
        given(sectionService.getSectionOrThrow(1L)).willReturn(section);
        given(teamRepository.save(any(Team.class))).willAnswer(inv -> {
            Team t = inv.getArgument(0);
            t.setId(2L);
            t.setStudents(new ArrayList<>());
            t.setInstructors(new ArrayList<>());
            return t;
        });

        TeamRequest req = new TeamRequest("Team Beta", "A new team", null, 1L);
        TeamResponse result = teamService.create(req);

        assertThat(result.name()).isEqualTo("Team Beta");
        then(teamRepository).should().save(any(Team.class));
    }

    @Test
    void create_givenDuplicateName_throwsIllegalStateException() {
        given(teamRepository.existsByName("Team Alpha")).willReturn(true);

        assertThatThrownBy(() -> teamService.create(new TeamRequest("Team Alpha", null, null, 1L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Team Alpha");
    }

    @Test
    void create_givenInvalidSectionId_throwsObjectNotFoundException() {
        given(teamRepository.existsByName("New Team")).willReturn(false);
        given(sectionService.getSectionOrThrow(99L))
                .willThrow(new ObjectNotFoundException("Section", 99L));

        assertThatThrownBy(() -> teamService.create(new TeamRequest("New Team", null, null, 99L)))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_givenValidRequest_updatesTeam() {
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamRepository.existsByName("Team Alpha Renamed")).willReturn(false);
        given(teamRepository.save(any(Team.class))).willReturn(team);

        TeamResponse result = teamService.update(1L,
                new TeamRequest("Team Alpha Renamed", "Updated desc", null, 1L));

        assertThat(result.name()).isEqualTo("Team Alpha Renamed");
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_givenValidId_deletesTeam() {
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));

        teamService.delete(1L);

        then(teamRepository).should().delete(team);
    }

    @Test
    void delete_givenInvalidId_throwsObjectNotFoundException() {
        given(teamRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.delete(99L))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    // ── assignStudents ────────────────────────────────────────────────────────

    @Test
    void assignStudents_givenValidRequest_assignsStudent() {
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(userRepository.findById(10L)).willReturn(Optional.of(student));
        given(userRepository.save(student)).willReturn(student);
        given(teamRepository.findById(1L)).willReturn(Optional.of(team)); // re-fetch after assignment

        teamService.assignStudents(1L, new AssignMembersRequest(List.of(10L)));

        then(userRepository).should().save(student);
    }

    @Test
    void assignStudents_givenNonStudent_throwsIllegalArgumentException() {
        AppUser instructor = new AppUser();
        instructor.setId(20L);
        instructor.setRole(UserRole.INSTRUCTOR);

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(userRepository.findById(20L)).willReturn(Optional.of(instructor));

        assertThatThrownBy(() -> teamService.assignStudents(1L,
                new AssignMembersRequest(List.of(20L))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a student");
    }
}
