package edu.tcu.cs.projectpulse.peerevaluation;

import edu.tcu.cs.projectpulse.activeweek.ActiveWeek;
import edu.tcu.cs.projectpulse.activeweek.ActiveWeekRepository;
import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.peerevaluation.dto.*;
import edu.tcu.cs.projectpulse.rubric.Criterion;
import edu.tcu.cs.projectpulse.rubric.CriterionRepository;
import edu.tcu.cs.projectpulse.user.AppUser;
import edu.tcu.cs.projectpulse.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PeerEvaluationService {

    private final PeerEvaluationRepository evalRepository;
    private final ActiveWeekRepository weekRepository;
    private final UserRepository userRepository;
    private final CriterionRepository criterionRepository;

    /**
     * Submit a peer evaluation for a teammate (UC-28).
     * Enforces BR-3: cannot edit once submitted.
     * Enforces BR-4: only previous week, within 1-week deadline.
     * Enforces BR-2: week must be active.
     */
    @PreAuthorize("hasRole('STUDENT')")
    @Transactional
    public PeerEvaluationResponse submit(PeerEvaluationRequest request) {
        AppUser evaluator = currentUser();
        AppUser evaluatee = userRepository.findById(request.evaluateeId())
                .orElseThrow(() -> new ObjectNotFoundException("User", request.evaluateeId()));
        ActiveWeek week = weekRepository.findById(request.weekId())
                .orElseThrow(() -> new ObjectNotFoundException("ActiveWeek", request.weekId()));

        // BR-2: week must be active
        if (!week.isActive()) {
            throw new IllegalArgumentException("Peer evaluations can only be submitted for active weeks.");
        }

        // BR-4: can only evaluate the previous week; deadline is 1 week after week start
        LocalDate today = LocalDate.now();
        LocalDate weekStart = week.getWeekStart();
        LocalDate deadline = weekStart.plusWeeks(2); // week starts Monday, submit window = that week + next
        if (!weekStart.isBefore(today) || today.isAfter(deadline)) {
            throw new IllegalArgumentException(
                    "Peer evaluations must be for a past week and submitted within 1 week of the week ending.");
        }

        // BR-3: cannot edit once submitted
        Optional<PeerEvaluation> existing = evalRepository
                .findByEvaluatorIdAndEvaluateeIdAndWeekId(
                        evaluator.getId(), evaluatee.getId(), week.getId());
        if (existing.isPresent() && existing.get().isSubmitted()) {
            throw new IllegalStateException("You have already submitted an evaluation for this teammate this week.");
        }

        // Cannot evaluate yourself
        if (evaluator.getId().equals(evaluatee.getId())) {
            throw new IllegalArgumentException("You cannot submit a peer evaluation for yourself.");
        }

        // Must be teammates
        if (evaluator.getTeam() == null || !evaluator.getTeam().equals(evaluatee.getTeam())) {
            throw new IllegalArgumentException("You can only evaluate members of your own team.");
        }

        PeerEvaluation eval = existing.orElseGet(PeerEvaluation::new);
        eval.setEvaluator(evaluator);
        eval.setEvaluatee(evaluatee);
        eval.setWeek(week);
        eval.setPublicComments(request.publicComments());
        eval.setPrivateComments(request.privateComments());
        eval.setSubmitted(true);
        eval.setSubmittedAt(LocalDateTime.now());

        // Map scores
        eval.getScores().clear();
        for (ScoreRequest sr : request.scores()) {
            Criterion criterion = criterionRepository.findById(sr.criterionId())
                    .orElseThrow(() -> new ObjectNotFoundException("Criterion", sr.criterionId()));
            EvaluationScore es = new EvaluationScore();
            es.setScore(sr.score());
            es.setCriterion(criterion);
            es.setPeerEvaluation(eval);
            eval.getScores().add(es);
        }

        return PeerEvaluationResponse.from(evalRepository.save(eval));
    }

    /**
     * Returns the student's own peer evaluation report for a week (UC-29).
     * BR-5: never exposes evaluator identity or private comments.
     */
    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public PeerEvalReportResponse getMyReport(Long weekId) {
        AppUser student = currentUser();
        ActiveWeek week = weekRepository.findById(weekId)
                .orElseThrow(() -> new ObjectNotFoundException("ActiveWeek", weekId));

        List<PeerEvaluation> evals = evalRepository
                .findByEvaluateeIdAndWeekId(student.getId(), weekId)
                .stream()
                .filter(PeerEvaluation::isSubmitted)
                .toList();

        if (evals.isEmpty()) {
            return new PeerEvalReportResponse(
                    weekId, student.getId(),
                    student.getFirstName() + " " + student.getLastName(),
                    0, BigDecimal.ZERO, List.of(), List.of());
        }

        // Collect all criterion IDs from the first evaluation's rubric
        // (all evals in a section use the same rubric)
        Map<Long, String> criterionNames = new LinkedHashMap<>();
        for (PeerEvaluation e : evals) {
            for (EvaluationScore s : e.getScores()) {
                criterionNames.putIfAbsent(s.getCriterion().getId(), s.getCriterion().getName());
            }
        }

        // Per-evaluator totals then average → overall grade
        List<BigDecimal> perEvaluatorTotals = evals.stream()
                .map(e -> e.getScores().stream()
                        .map(s -> BigDecimal.valueOf(s.getScore()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .toList();

        BigDecimal overallGrade = perEvaluatorTotals.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(perEvaluatorTotals.size()), 2, RoundingMode.HALF_UP);

        // Per-criterion averages
        List<CriterionReportDto> criterionAverages = criterionNames.entrySet().stream()
                .map(entry -> {
                    Long cId = entry.getKey();
                    List<Integer> scores = evals.stream()
                            .flatMap(e -> e.getScores().stream())
                            .filter(s -> s.getCriterion().getId().equals(cId))
                            .map(EvaluationScore::getScore)
                            .toList();
                    BigDecimal avg = scores.isEmpty() ? BigDecimal.ZERO
                            : BigDecimal.valueOf(scores.stream().mapToInt(Integer::intValue).sum())
                            .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
                    // Get maxScore from criterion
                    BigDecimal maxScore = evals.stream()
                            .flatMap(e -> e.getScores().stream())
                            .filter(s -> s.getCriterion().getId().equals(cId))
                            .map(s -> s.getCriterion().getMaxScore())
                            .findFirst().orElse(BigDecimal.TEN);
                    return new CriterionReportDto(cId, entry.getValue(), avg, maxScore);
                })
                .toList();

        // Public comments only — no evaluator identity (BR-5)
        List<String> publicComments = evals.stream()
                .map(PeerEvaluation::getPublicComments)
                .filter(c -> c != null && !c.isBlank())
                .toList();

        return new PeerEvalReportResponse(
                weekId,
                student.getId(),
                student.getFirstName() + " " + student.getLastName(),
                evals.size(),
                overallGrade,
                criterionAverages,
                publicComments
        );
    }

    // ── private ───────────────────────────────────────────────────────────────

    private AppUser currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ObjectNotFoundException("Current user not found."));
    }
}
