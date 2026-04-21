package edu.tcu.cs.projectpulse.user;

import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.email.EmailService;
import edu.tcu.cs.projectpulse.invitation.InvitationTokenRepository;
import edu.tcu.cs.projectpulse.section.SectionService;
import edu.tcu.cs.projectpulse.user.dto.UpdateAccountRequest;
import edu.tcu.cs.projectpulse.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock InvitationTokenRepository tokenRepository;
    @Mock SectionService sectionService;
    @Mock EmailService emailService;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    private AppUser student;
    private AppUser instructor;

    @BeforeEach
    void setUp() {
        student = new AppUser();
        student.setId(1L);
        student.setFirstName("Alice");
        student.setLastName("Smith");
        student.setEmail("alice@tcu.edu");
        student.setRole(UserRole.STUDENT);
        student.setActive(true);

        instructor = new AppUser();
        instructor.setId(2L);
        instructor.setFirstName("Bob");
        instructor.setLastName("Jones");
        instructor.setEmail("bob@tcu.edu");
        instructor.setRole(UserRole.INSTRUCTOR);
        instructor.setActive(true);

        // Set up SecurityContext so currentUser() works
        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("alice@tcu.edu");
        SecurityContext ctx = mock(SecurityContext.class);
        given(ctx.getAuthentication()).willReturn(auth);
        SecurityContextHolder.setContext(ctx);

        given(userRepository.findByEmail("alice@tcu.edu")).willReturn(Optional.of(student));
    }

    // ── getMe ─────────────────────────────────────────────────────────────────

    @Test
    void getMe_returnsCurrentUser() {
        UserResponse result = userService.getMe();

        assertThat(result.email()).isEqualTo("alice@tcu.edu");
        assertThat(result.firstName()).isEqualTo("Alice");
    }

    // ── updateMe ──────────────────────────────────────────────────────────────

    @Test
    void updateMe_givenValidRequest_updatesNameOnly() {
        given(userRepository.save(student)).willReturn(student);

        UpdateAccountRequest req = new UpdateAccountRequest("Alicia", "Smith", null, null, null);
        UserResponse result = userService.updateMe(req);

        assertThat(result.firstName()).isEqualTo("Alicia");
        then(passwordEncoder).shouldHaveNoInteractions();
    }

    @Test
    void updateMe_givenNewPassword_encodesAndSaves() {
        given(passwordEncoder.encode("NewPass1!")).willReturn("hashed");
        given(userRepository.save(student)).willReturn(student);

        UpdateAccountRequest req = new UpdateAccountRequest("Alice", "Smith", null, "NewPass1!", "NewPass1!");
        userService.updateMe(req);

        then(passwordEncoder).should().encode("NewPass1!");
        assertThat(student.getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void updateMe_givenMismatchedPasswords_throwsIllegalArgumentException() {
        UpdateAccountRequest req = new UpdateAccountRequest("Alice", "Smith", null, "NewPass1!", "Different!");

        assertThatThrownBy(() -> userService.updateMe(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("do not match");
    }

    // ── findStudents ──────────────────────────────────────────────────────────

    @Test
    void findStudents_givenNoFilter_returnsAllStudents() {
        given(userRepository.findByRole(UserRole.STUDENT)).willReturn(List.of(student));

        List<UserResponse> result = userService.findStudents(null, null, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("alice@tcu.edu");
    }

    @Test
    void findStudents_givenLastNameFilter_filtersResults() {
        AppUser other = new AppUser();
        other.setId(99L);
        other.setLastName("Brown");
        other.setFirstName("Charlie");
        other.setEmail("charlie@tcu.edu");
        other.setRole(UserRole.STUDENT);

        given(userRepository.findByRole(UserRole.STUDENT)).willReturn(List.of(student, other));

        List<UserResponse> result = userService.findStudents(null, "Smith", null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).lastName()).isEqualTo("Smith");
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findById_givenValidId_returnsUser() {
        given(userRepository.findById(1L)).willReturn(Optional.of(student));

        UserResponse result = userService.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findById_givenInvalidId_throwsObjectNotFoundException() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    // ── deactivate / reactivate ───────────────────────────────────────────────

    @Test
    void deactivate_givenInstructor_setsActiveFalse() {
        given(userRepository.findById(2L)).willReturn(Optional.of(instructor));
        given(userRepository.save(instructor)).willReturn(instructor);

        UserResponse result = userService.deactivate(2L);

        assertThat(result.active()).isFalse();
    }

    @Test
    void deactivate_givenStudent_throwsIllegalArgumentException() {
        given(userRepository.findById(1L)).willReturn(Optional.of(student));

        assertThatThrownBy(() -> userService.deactivate(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("instructor");
    }

    @Test
    void reactivate_givenInstructor_setsActiveTrue() {
        instructor.setActive(false);
        given(userRepository.findById(2L)).willReturn(Optional.of(instructor));
        given(userRepository.save(instructor)).willReturn(instructor);

        UserResponse result = userService.reactivate(2L);

        assertThat(result.active()).isTrue();
    }

    // ── deleteStudent ─────────────────────────────────────────────────────────

    @Test
    void deleteStudent_givenValidStudent_deletesUser() {
        given(userRepository.findById(1L)).willReturn(Optional.of(student));

        userService.deleteStudent(1L);

        then(userRepository).should().delete(student);
    }

    @Test
    void deleteStudent_givenNonStudent_throwsIllegalArgumentException() {
        given(userRepository.findById(2L)).willReturn(Optional.of(instructor));

        assertThatThrownBy(() -> userService.deleteStudent(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("student");
    }
}
