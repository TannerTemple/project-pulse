package edu.tcu.cs.projectpulse.report;

import edu.tcu.cs.projectpulse.activeweek.ActiveWeek;
import edu.tcu.cs.projectpulse.activeweek.ActiveWeekRepository;
import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.peerevaluation.EvaluationScore;
import edu.tcu.cs.projectpulse.peerevaluation.PeerEvaluation;
import edu.tcu.cs.projectpulse.peerevaluation.PeerEvaluationRepository;
import edu.tcu.cs.projectpulse.report.dto.SectionPeerReportResponse;
import edu.tcu.cs.projectpulse.report.dto.StudentPeerRangeResponse;
import edu.tcu.cs.projectpulse.report.dto.StudentWARRangeResponse;
import edu.tcu.cs.projectpulse.report.dto.TeamWARReportResponse;
import edu.tcu.cs.projectpulse.rubric.Criterion;
import edu.tcu.cs.projectpulse.section.Section;
import edu.tcu.cs.projectpulse.section.SectionRepository;
import edu.tcu.cs.projectpulse.team.Team;
import edu.tcu.cs.projectpulse.team.TeamRepository;
import edu.tcu.cs.projectpulse.user.AppUser;
import edu.tcu.cs.projectpulse.user.UserRepository;
import edu.tcu.cs.projectpulse.user.UserRole;
import edu.tcu.cs.projectpulse.war.WARActivity;
import edu.tcu.cs.projectpulse.war.WARActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock SectionRepository sectionRepository;
    @Mock TeamRepository teamRepository;
    @Mock UserRepository userRepository;
    @Mock ActiveWeekRepository weekRepository;
    @Mock PeerEvaluationRepository evalRepository;
    @Mock WARActivityRepository warRepository;
    @InjectMocks ReportService reportService;

    private Section section;
    private Team team;
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
        week.setSection(section);

        alice = new AppUser();
        alice.setId(1L);
        alice.setFirstName("Alice");
        alice.setLastName("Smith");
        alice.setEmail("alice@tcu.edu");
        alice.setRole(UserRole.STUDENT);
        alice.setSection(section);

        bob = new AppUser();
        bob.setId(2L);
        bob.setFirstName("Bob");
        bob.setLastName("Jones");
        bob.setEmail("bob@tcu.edu");
        bob.setRole(UserRole.STUDENT);
        bob.setSection(section);

        team = new Team();
        team.setId(1L);
        team.setName("Team Alpha");
        team.setSection(section);
        team.setStudents(new ArrayList<>(List.of(alice)));
        team.setInstructors(new ArrayList<>());
    }

    // ── sectionPeerReport (UC-31) ─────────────────────────────────────────────

    @Test
    void sectionPeerReport_givenValidIds_returnsReport() {
        given(sectionRepository.findById(1L)).willReturn(Optional.of(section));
        given(weekRepository.findById(10L)).willReturn(Optional.of(week));
        given(userRepository.findByRole(UserRole.STUDENT)).willReturn(List.of(alice));
        given(evalRepository.findByWeekId(10L)).willReturn(List.of());

        SectionPeerReportResponse result = reportService.sectionPeerReport(1L, 10L);

        assertThat(result.sectionId()).isEqualTo(1L);
        assertThat(result.sectionName()).isEqualTo("2024-2025");
        assertThat(result.weekId()).isEqualTo(10L);
        assertThat(result.students()).hasSize(1);
    }

    @Test
    void sectionPeerReport_identifiesNonSubmitters() {
        // alice evaluated bob, so bob submitted — alice did not
        PeerEvaluation eval = submittedEval(bob, alice, week, 8);

        given(sectionRepository.findById(1L)).willReturn(Optional.of(section));
        given(weekRepository.findById(10L)).willReturn(Optional.of(week));
        given(userRepository.findByRole(UserRole.STUDENT)).willReturn(List.of(alice, bob));
        given(evalRepository.findByWeekId(10L)).willReturn(List.of(eval));

        SectionPeerReportResponse result = reportService.sectionPeerReport(1L, 10L);

        // bob submitted (is evaluator), alice did not
        assertThat(result.nonSubmitters()).containsExactly("Alice Smith");
    }

    @Test
    void sectionPeerReport_givenInvalidSection_throwsObjectNotFoundException() {
        given(sectionRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.sectionPeerReport(99L, 10L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void sectionPeerReport_givenInvalidWeek_throwsObjectNotFoundException() {
        given(sectionRepository.findById(1L)).willReturn(Optional.of(section));
        given(weekRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.sectionPeerReport(1L, 99L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── teamWARReport (UC-32) ─────────────────────────────────────────────────

    @Test
    void teamWARReport_givenValidIds_returnsReport() {
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(weekRepository.findById(10L)).willReturn(Optional.of(week));
        given(warRepository.findByStudentIdAndWeekId(1L, 10L)).willReturn(List.of(warActivity(alice, week)));

        TeamWARReportResponse result = reportService.teamWARReport(1L, 10L);

        assertThat(result.teamId()).isEqualTo(1L);
        assertThat(result.teamName()).isEqualTo("Team Alpha");
        assertThat(result.entries()).hasSize(1);
        assertThat(result.nonSubmitters()).isEmpty();
    }

    @Test
    void teamWARReport_identifiesNonSubmitters() {
        team.setStudents(new ArrayList<>(List.of(alice, bob)));
        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(weekRepository.findById(10L)).willReturn(Optional.of(week));
        given(warRepository.findByStudentIdAndWeekId(1L, 10L)).willReturn(List.of(warActivity(alice, week)));
        given(warRepository.findByStudentIdAndWeekId(2L, 10L)).willReturn(List.of());

        TeamWARReportResponse result = reportService.teamWARReport(1L, 10L);

        assertThat(result.nonSubmitters()).containsExactly("Bob Jones");
    }

    @Test
    void teamWARReport_givenInvalidTeam_throwsObjectNotFoundException() {
        given(teamRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.teamWARReport(99L, 10L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── studentPeerReport (UC-33) ─────────────────────────────────────────────

    @Test
    void studentPeerReport_givenValidId_returnsReportWithinDateRange() {
        PeerEvaluation eval = submittedEval(bob, alice, week, 8);

        given(userRepository.findById(1L)).willReturn(Optional.of(alice));
        given(evalRepository.findByEvaluateeId(1L)).willReturn(List.of(eval));

        StudentPeerRangeResponse result = reportService.studentPeerReport(
                1L, LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 31));

        assertThat(result.studentId()).isEqualTo(1L);
        assertThat(result.studentName()).isEqualTo("Alice Smith");
        assertThat(result.weeks()).hasSize(1);
    }

    @Test
    void studentPeerReport_excludesEvalsOutsideDateRange() {
        PeerEvaluation eval = submittedEval(bob, alice, week, 8); // week starts 2024-10-07

        given(userRepository.findById(1L)).willReturn(Optional.of(alice));
        given(evalRepository.findByEvaluateeId(1L)).willReturn(List.of(eval));

        // Range that does NOT include Oct 7
        StudentPeerRangeResponse result = reportService.studentPeerReport(
                1L, LocalDate.of(2024, 11, 1), LocalDate.of(2024, 11, 30));

        assertThat(result.weeks()).isEmpty();
    }

    @Test
    void studentPeerReport_givenInvalidId_throwsObjectNotFoundException() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.studentPeerReport(
                99L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    // ── studentWARReport (UC-34) ──────────────────────────────────────────────

    @Test
    void studentWARReport_givenValidId_returnsActivitiesInRange() {
        WARActivity activity = warActivity(alice, week); // week starts 2024-10-07

        given(userRepository.findById(1L)).willReturn(Optional.of(alice));
        given(warRepository.findByStudentId(1L)).willReturn(List.of(activity));

        StudentWARRangeResponse result = reportService.studentWARReport(
                1L, LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 31));

        assertThat(result.studentId()).isEqualTo(1L);
        assertThat(result.studentName()).isEqualTo("Alice Smith");
        assertThat(result.weeks()).hasSize(1);
    }

    @Test
    void studentWARReport_excludesActivitiesOutsideDateRange() {
        WARActivity activity = warActivity(alice, week); // week starts 2024-10-07

        given(userRepository.findById(1L)).willReturn(Optional.of(alice));
        given(warRepository.findByStudentId(1L)).willReturn(List.of(activity));

        StudentWARRangeResponse result = reportService.studentWARReport(
                1L, LocalDate.of(2024, 11, 1), LocalDate.of(2024, 11, 30));

        assertThat(result.weeks()).isEmpty();
    }

    @Test
    void studentWARReport_givenInvalidId_throwsObjectNotFoundException() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.studentWARReport(
                99L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private PeerEvaluation submittedEval(AppUser evaluator, AppUser evaluatee,
                                         ActiveWeek evalWeek, int score) {
        Criterion criterion = new Criterion();
        criterion.setId(1L);
        criterion.setName("Quality");

        EvaluationScore evalScore = new EvaluationScore();
        evalScore.setId(1L);
        evalScore.setScore(score);
        evalScore.setCriterion(criterion);

        PeerEvaluation eval = new PeerEvaluation();
        eval.setId(1L);
        eval.setEvaluator(evaluator);
        eval.setEvaluatee(evaluatee);
        eval.setWeek(evalWeek);
        eval.setSubmitted(true);
        eval.setPublicComments("Good work");
        eval.setPrivateComments("Private note");
        eval.setScores(new ArrayList<>(List.of(evalScore)));
        evalScore.setPeerEvaluation(eval);
        return eval;
    }

    private WARActivity warActivity(AppUser student, ActiveWeek actWeek) {
        WARActivity a = new WARActivity();
        a.setId(1L);
        a.setStudent(student);
        a.setWeek(actWeek);
        a.setActivity("Write unit tests");
        a.setDescription("Testing service layer");
        a.setPlannedHours(4.0);
        a.setActualHours(3.5);
        return a;
    }
}
