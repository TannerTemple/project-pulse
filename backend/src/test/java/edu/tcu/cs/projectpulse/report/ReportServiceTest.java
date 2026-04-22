package edu.tcu.cs.projectpulse.report;

import edu.tcu.cs.projectpulse.activeweek.ActiveWeek;
import edu.tcu.cs.projectpulse.activeweek.ActiveWeekRepository;
import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.peerevaluation.EvaluationScore;
import edu.tcu.cs.projectpulse.peerevaluation.PeerEvaluation;
import edu.tcu.cs.projectpulse.peerevaluation.PeerEvaluationRepository;
import edu.tcu.cs.projectpulse.report.dto.SectionPeerReportResponse;
import edu.tcu.cs.projectpulse.report.dto.StudentPeerSummary;
import edu.tcu.cs.projectpulse.rubric.Criterion;
import edu.tcu.cs.projectpulse.section.Section;
import edu.tcu.cs.projectpulse.section.SectionRepository;
import edu.tcu.cs.projectpulse.team.Team;
import edu.tcu.cs.projectpulse.team.TeamRepository;
import edu.tcu.cs.projectpulse.user.AppUser;
import edu.tcu.cs.projectpulse.user.UserRepository;
import edu.tcu.cs.projectpulse.user.UserRole;
import edu.tcu.cs.projectpulse.war.WARActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private SectionRepository sectionRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private UserRepository userRepository;
    @Mock private ActiveWeekRepository weekRepository;
    @Mock private PeerEvaluationRepository evalRepository;
    @Mock private WARActivityRepository warRepository;

    @InjectMocks private ReportService reportService;

    private Section section;
    private ActiveWeek week;
    private AppUser alice;
    private AppUser bob;

    @BeforeEach
    void setUp() {
        section = new Section();
        section.setId(1L);
        section.setName("2024-2025");

        week = new ActiveWeek();
        week.setId(10L);
        week.setWeekStart(LocalDate.of(2024, 10, 7));

        alice = student(101L, "Alice", "Anderson", section);
        bob = student(102L, "Bob", "Brown", section);
    }

    @Test
    void sectionPeerReport_givenMissingSection_throwsObjectNotFoundException() {
        given(sectionRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.sectionPeerReport(99L, 10L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("Section");
    }

    @Test
    void sectionPeerReport_givenValidData_returnsSummariesAndNonSubmitters() {
        PeerEvaluation bobReceived = submittedEval(alice, bob, week, 8, 6);

        given(sectionRepository.findById(1L)).willReturn(Optional.of(section));
        given(weekRepository.findById(10L)).willReturn(Optional.of(week));
        given(userRepository.findByRole(UserRole.STUDENT)).willReturn(List.of(alice, bob));
        given(evalRepository.findByWeekId(10L)).willReturn(List.of(bobReceived));

        SectionPeerReportResponse result = reportService.sectionPeerReport(1L, 10L);

        assertThat(result.sectionName()).isEqualTo("2024-2025");
        assertThat(result.students()).hasSize(2);
        assertThat(result.nonSubmitters()).containsExactly("Bob Brown");

        StudentPeerSummary bobSummary = result.students().stream()
                .filter(s -> s.studentId().equals(102L))
                .findFirst()
                .orElseThrow();

        assertThat(bobSummary.submitted()).isTrue();
        assertThat(bobSummary.grade()).isEqualByComparingTo(new BigDecimal("14.00"));
        assertThat(bobSummary.evaluations()).hasSize(1);
        assertThat(bobSummary.evaluations().get(0).evaluatorName()).isEqualTo("Alice Anderson");
        assertThat(bobSummary.evaluations().get(0).privateComments()).isEqualTo("Instructor-only");
    }

    @Test
    void teamWARReport_givenMissingTeam_throwsObjectNotFoundException() {
        given(teamRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.teamWARReport(999L, 10L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("Team");
    }

    @Test
    void studentPeerReport_givenMissingStudent_throwsObjectNotFoundException() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.studentPeerReport(
                999L, LocalDate.of(2024, 9, 1), LocalDate.of(2024, 12, 31)))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("User");
    }

    private static AppUser student(Long id, String firstName, String lastName, Section section) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(UserRole.STUDENT);
        user.setSection(section);
        return user;
    }

    private static PeerEvaluation submittedEval(
            AppUser evaluator, AppUser evaluatee, ActiveWeek week, int... scores) {
        PeerEvaluation eval = new PeerEvaluation();
        eval.setEvaluator(evaluator);
        eval.setEvaluatee(evaluatee);
        eval.setWeek(week);
        eval.setSubmitted(true);
        eval.setPublicComments("Public");
        eval.setPrivateComments("Instructor-only");

        List<EvaluationScore> scoreRows = java.util.Arrays.stream(scores)
                .mapToObj(score -> {
                    EvaluationScore row = new EvaluationScore();
                    row.setScore(score);
                    Criterion criterion = new Criterion();
                    criterion.setName("Criterion");
                    row.setCriterion(criterion);
                    return row;
                })
                .toList();
        eval.setScores(scoreRows);

        return eval;
    }
}
