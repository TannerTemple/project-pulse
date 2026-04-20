package edu.tcu.cs.projectpulse.war;

import edu.tcu.cs.projectpulse.activeweek.ActiveWeek;
import edu.tcu.cs.projectpulse.activeweek.ActiveWeekRepository;
import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.user.AppUser;
import edu.tcu.cs.projectpulse.user.UserRepository;
import edu.tcu.cs.projectpulse.war.dto.WARActivityRequest;
import edu.tcu.cs.projectpulse.war.dto.WARActivityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class WARActivityServiceTest {

    @Mock WARActivityRepository warRepository;
    @Mock ActiveWeekRepository  weekRepository;
    @Mock UserRepository        userRepository;
    @InjectMocks WARActivityService warService;

    private AppUser student;
    private ActiveWeek pastWeek;

    @BeforeEach
    void setUp() {
        student = new AppUser();
        student.setId(1L);
        student.setFirstName("Alice");
        student.setLastName("Smith");
        student.setEmail("alice@tcu.edu");

        pastWeek = new ActiveWeek();
        pastWeek.setId(10L);
        pastWeek.setWeekStart(LocalDate.now().minusWeeks(1));
        pastWeek.setActive(true);

        // Mock security context
        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("alice@tcu.edu");
        SecurityContext ctx = mock(SecurityContext.class);
        given(ctx.getAuthentication()).willReturn(auth);
        SecurityContextHolder.setContext(ctx);

        given(userRepository.findByEmail("alice@tcu.edu")).willReturn(Optional.of(student));
    }

    // ── findMyActivities ──────────────────────────────────────────────────────

    @Test
    void findMyActivities_givenValidWeek_returnsStudentsActivities() {
        WARActivity activity = activityFor(student, pastWeek);
        given(warRepository.findByStudentIdAndWeekId(1L, 10L)).willReturn(List.of(activity));

        List<WARActivityResponse> result = warService.findMyActivities(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).activity()).isEqualTo("Build login module");
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_givenValidRequest_savesActivity() {
        given(weekRepository.findById(10L)).willReturn(Optional.of(pastWeek));
        given(warRepository.save(any(WARActivity.class))).willAnswer(inv -> {
            WARActivity a = inv.getArgument(0);
            a.setId(99L);
            return a;
        });

        WARActivityRequest req = new WARActivityRequest(
                ActivityCategory.DEVELOPMENT, "Build login module", "OAuth flow",
                4.0, null, ActivityStatus.IN_PROGRESS, 10L
        );

        WARActivityResponse result = warService.create(req);

        assertThat(result.activity()).isEqualTo("Build login module");
        assertThat(result.category()).isEqualTo(ActivityCategory.DEVELOPMENT);
        then(warRepository).should().save(any(WARActivity.class));
    }

    @Test
    void create_givenFutureWeek_throwsIllegalArgumentException() {
        ActiveWeek futureWeek = new ActiveWeek();
        futureWeek.setId(20L);
        futureWeek.setWeekStart(LocalDate.now().plusWeeks(1));
        given(weekRepository.findById(20L)).willReturn(Optional.of(futureWeek));

        WARActivityRequest req = new WARActivityRequest(
                ActivityCategory.TESTING, "Some test", null,
                2.0, null, ActivityStatus.IN_PROGRESS, 20L
        );

        assertThatThrownBy(() -> warService.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future week");
    }

    @Test
    void create_givenInvalidWeekId_throwsObjectNotFoundException() {
        given(weekRepository.findById(999L)).willReturn(Optional.empty());

        WARActivityRequest req = new WARActivityRequest(
                ActivityCategory.DESIGN, "Mock", null, 1.0, null, ActivityStatus.DONE, 999L
        );

        assertThatThrownBy(() -> warService.create(req))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_givenOwnActivity_deletesSuccessfully() {
        WARActivity activity = activityFor(student, pastWeek);
        activity.setId(5L);
        given(warRepository.findById(5L)).willReturn(Optional.of(activity));

        warService.delete(5L);

        then(warRepository).should().delete(activity);
    }

    @Test
    void delete_givenOtherStudentsActivity_throwsIllegalArgumentException() {
        AppUser other = new AppUser();
        other.setId(999L);
        WARActivity activity = activityFor(other, pastWeek);
        activity.setId(5L);
        given(warRepository.findById(5L)).willReturn(Optional.of(activity));

        assertThatThrownBy(() -> warService.delete(5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private WARActivity activityFor(AppUser owner, ActiveWeek week) {
        WARActivity a = new WARActivity();
        a.setStudent(owner);
        a.setWeek(week);
        a.setCategory(ActivityCategory.DEVELOPMENT);
        a.setActivity("Build login module");
        a.setPlannedHours(4.0);
        a.setStatus(ActivityStatus.IN_PROGRESS);
        return a;
    }
}
