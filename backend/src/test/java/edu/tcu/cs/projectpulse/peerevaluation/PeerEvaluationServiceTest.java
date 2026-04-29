package edu.tcu.cs.projectpulse.peerevaluation;

import edu.tcu.cs.projectpulse.activeweek.ActiveWeek;
import edu.tcu.cs.projectpulse.activeweek.ActiveWeekRepository;
import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.peerevaluation.dto.PeerEvalReportResponse;
import edu.tcu.cs.projectpulse.peerevaluation.dto.PeerEvaluationRequest;
import edu.tcu.cs.projectpulse.peerevaluation.dto.ScoreRequest;
import edu.tcu.cs.projectpulse.rubric.Criterion;
import edu.tcu.cs.projectpulse.rubric.CriterionRepository;
import edu.tcu.cs.projectpulse.team.Team;
import edu.tcu.cs.projectpulse.user.AppUser;
import edu.tcu.cs.projectpulse.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PeerEvaluationServiceTest {

    @Mock PeerEvaluationRepository evalRepository;
    @Mock ActiveWeekRepository     weekRepository;
    @Mock UserRepository           userRepository;
    @Mock CriterionRepository      criterionRepository;
    @InjectMocks PeerEvaluationService evalService;

    private AppUser evaluator;
    private AppUser evaluatee;
    private Team    team;
    private ActiveWeek previousWeek;
    private Criterion  criterion;

    @BeforeEach
    void setUp() {
        team = new Team();
        team.setId(1L);
        team.setName("Team Alpha");

        evaluator = new AppUser();
        evaluator.setId(1L);
        evaluator.setFirstName("Alice");
        evaluator.setLastName("Smith");
        evaluator.setEmail("alice@tcu.edu");
        evaluator.setTeam(team);

        evaluatee = new AppUser();
        evaluatee.setId(2L);
        evaluatee.setFirstName("Bob");
        evaluatee.setLastName("Jones");
        evaluatee.setEmail("bob@tcu.edu");
        evaluatee.setTeam(team);

        // Previous week — valid for evaluation
        previousWeek = new ActiveWeek();
        previousWeek.setId(10L);
        previousWeek.setWeekStart(LocalDate.now().minusWeeks(1));
        previousWeek.setActive(true);

        criterion = new Criterion();
        criterion.setId(1L);
        criterion.setName("Quality of Work");
        criterion.setMaxScore(new BigDecimal("10"));

        // Mock security context
        Authentication auth = mock(Authentication.class);
        given(auth.getName()).willReturn("alice@tcu.edu");
        SecurityContext ctx = mock(SecurityContext.class);
        given(ctx.getAuthentication()).willReturn(auth);
        SecurityContextHolder.setContext(ctx);

        given(userRepository.findByEmail("alice@tcu.edu")).willReturn(Optional.of(evaluator));
    }

    // ── submit ────────────────────────────────────────────────────────────────

    @Test
    void submit_givenValidRequest_savesEvaluation() {
        given(userRepository.findById(2L)).willReturn(Optional.of(evaluatee));
        given(weekRepository.findById(10L)).willReturn(Optional.of(previousWeek));
        given(evalRepository.findByEvaluatorIdAndEvaluateeIdAndWeekId(1L, 2L, 10L))
                .willReturn(Optional.empty());
        given(criterionRepository.findById(1L)).willReturn(Optional.of(criterion));
        given(evalRepository.save(any(PeerEvaluation.class))).willAnswer(inv -> {
            PeerEvaluation e = inv.getArgument(0);
            e.setId(99L);
            return e;
        });

        PeerEvaluationRequest req = new PeerEvaluationRequest(
                2L, 10L, "Great work!", null,
                List.of(new ScoreRequest(1L, 9))
        );

        var result = evalService.submit(req);

        assertThat(result.submitted()).isTrue();
        assertThat(result.evaluateeName()).isEqualTo("Bob Jones");
        then(evalRepository).should().save(any(PeerEvaluation.class));
    }

    @Test
    void submit_givenAlreadySubmitted_throwsIllegalStateException() {
        PeerEvaluation existing = new PeerEvaluation();
        existing.setSubmitted(true);
        given(userRepository.findById(2L)).willReturn(Optional.of(evaluatee));
        given(weekRepository.findById(10L)).willReturn(Optional.of(previousWeek));
        given(evalRepository.findByEvaluatorIdAndEvaluateeIdAndWeekId(1L, 2L, 10L))
                .willReturn(Optional.of(existing));

        PeerEvaluationRequest req = new PeerEvaluationRequest(
                2L, 10L, null, null, List.of(new ScoreRequest(1L, 8))
        );

        assertThatThrownBy(() -> evalService.submit(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already submitted");
    }

    @Test
    void submit_givenInactiveWeek_throwsIllegalArgumentException() {
        previousWeek.setActive(false);
        given(userRepository.findById(2L)).willReturn(Optional.of(evaluatee));
        given(weekRepository.findById(10L)).willReturn(Optional.of(previousWeek));

        PeerEvaluationRequest req = new PeerEvaluationRequest(
                2L, 10L, null, null, List.of(new ScoreRequest(1L, 7))
        );

        assertThatThrownBy(() -> evalService.submit(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("active");
    }

    @Test
    void submit_givenSelfEvaluation_throwsIllegalArgumentException() {
        given(userRepository.findById(1L)).willReturn(Optional.of(evaluator));
        given(weekRepository.findById(10L)).willReturn(Optional.of(previousWeek));
        given(evalRepository.findByEvaluatorIdAndEvaluateeIdAndWeekId(1L, 1L, 10L))
                .willReturn(Optional.empty());

        PeerEvaluationRequest req = new PeerEvaluationRequest(
                1L, 10L, null, null, List.of(new ScoreRequest(1L, 10))
        );

        assertThatThrownBy(() -> evalService.submit(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("yourself");
    }

    @Test
    void submit_givenNonTeammate_throwsIllegalArgumentException() {
        AppUser stranger = new AppUser();
        stranger.setId(3L);
        stranger.setTeam(new Team()); // different team
        given(userRepository.findById(3L)).willReturn(Optional.of(stranger));
        given(weekRepository.findById(10L)).willReturn(Optional.of(previousWeek));
        given(evalRepository.findByEvaluatorIdAndEvaluateeIdAndWeekId(1L, 3L, 10L))
                .willReturn(Optional.empty());

        PeerEvaluationRequest req = new PeerEvaluationRequest(
                3L, 10L, null, null, List.of(new ScoreRequest(1L, 5))
        );

        assertThatThrownBy(() -> evalService.submit(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("team");
    }

    // ── getMyReport ───────────────────────────────────────────────────────────

    @Test
    void getMyReport_givenNoEvaluations_returnsZeroGrade() {
        given(weekRepository.findById(10L)).willReturn(Optional.of(previousWeek));
        given(evalRepository.findByEvaluateeIdAndWeekId(1L, 10L)).willReturn(List.of());

        PeerEvalReportResponse result = evalService.getMyReport(10L);

        assertThat(result.evaluatorCount()).isZero();
        assertThat(result.overallGrade()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.publicComments()).isEmpty();
    }

    @Test
    void getMyReport_givenEvaluations_computesGradeAndHidesPrivateData() {
        PeerEvaluation eval = buildSubmittedEval(evaluatee, evaluator, previousWeek, 8, "Good job", "Private note");
        given(weekRepository.findById(10L)).willReturn(Optional.of(previousWeek));
        given(evalRepository.findByEvaluateeIdAndWeekId(1L, 10L)).willReturn(List.of(eval));

        PeerEvalReportResponse result = evalService.getMyReport(10L);

        assertThat(result.evaluatorCount()).isEqualTo(1);
        assertThat(result.publicComments()).contains("Good job");
        // Private comments must NOT be exposed (BR-5)
        assertThat(result.publicComments()).doesNotContain("Private note");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private PeerEvaluation buildSubmittedEval(AppUser evaluatee, AppUser evaluator,
                                               ActiveWeek week, int score,
                                               String pub, String priv) {
        EvaluationScore es = new EvaluationScore();
        es.setScore(score);
        es.setCriterion(criterion);

        PeerEvaluation e = new PeerEvaluation();
        e.setEvaluator(evaluator);
        e.setEvaluatee(evaluatee);
        e.setWeek(week);
        e.setSubmitted(true);
        e.setPublicComments(pub);
        e.setPrivateComments(priv);
        e.setScores(new ArrayList<>(List.of(es)));
        es.setPeerEvaluation(e);
        return e;
    }
}
