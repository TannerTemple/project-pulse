package edu.tcu.cs.projectpulse.report;

import edu.tcu.cs.projectpulse.activeweek.ActiveWeek;
import edu.tcu.cs.projectpulse.activeweek.ActiveWeekRepository;
import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.peerevaluation.EvaluationScore;
import edu.tcu.cs.projectpulse.peerevaluation.PeerEvaluation;
import edu.tcu.cs.projectpulse.peerevaluation.PeerEvaluationRepository;
import edu.tcu.cs.projectpulse.report.dto.*;
import edu.tcu.cs.projectpulse.section.Section;
import edu.tcu.cs.projectpulse.section.SectionRepository;
import edu.tcu.cs.projectpulse.team.Team;
import edu.tcu.cs.projectpulse.team.TeamRepository;
import edu.tcu.cs.projectpulse.user.AppUser;
import edu.tcu.cs.projectpulse.user.UserRepository;
import edu.tcu.cs.projectpulse.user.UserRole;
import edu.tcu.cs.projectpulse.war.WARActivity;
import edu.tcu.cs.projectpulse.war.WARActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SectionRepository sectionRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final ActiveWeekRepository weekRepository;
    private final PeerEvaluationRepository evalRepository;
    private final WARActivityRepository warRepository;

    // ── UC-31: Section peer evaluation report ────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Transactional(readOnly = true)
    public SectionPeerReportResponse sectionPeerReport(Long sectionId, Long weekId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ObjectNotFoundException("Section", sectionId));
        ActiveWeek week = weekRepository.findById(weekId)
                .orElseThrow(() -> new ObjectNotFoundException("ActiveWeek", weekId));

        // All students in this section
        List<AppUser> students = userRepository.findByRole(UserRole.STUDENT).stream()
                .filter(u -> u.getSection() != null && u.getSection().getId().equals(sectionId))
                .sorted(Comparator.comparing(AppUser::getLastName))
                .toList();

        // All submitted evals for this week (entire section)
        List<PeerEvaluation> allEvals = evalRepository.findByWeekId(weekId).stream()
                .filter(PeerEvaluation::isSubmitted)
                .toList();

        // Students who submitted at least one evaluation
        Set<Long> submitterIds = allEvals.stream()
                .map(e -> e.getEvaluator().getId())
                .collect(Collectors.toSet());

        List<String> nonSubmitters = students.stream()
                .filter(s -> !submitterIds.contains(s.getId()))
                .map(s -> s.getFirstName() + " " + s.getLastName())
                .toList();

        // Build per-student summary (as evaluatee)
        List<StudentPeerSummary> summaries = students.stream()
                .map(student -> buildStudentSummary(student, allEvals))
                .toList();

        return new SectionPeerReportResponse(
                sectionId, section.getName(),
                weekId, week.getWeekStart().toString(),
                summaries, nonSubmitters
        );
    }

    // ── UC-32: Team WAR report ────────────────────────────────────────────────

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Transactional(readOnly = true)
    public TeamWARReportResponse teamWARReport(Long teamId, Long weekId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ObjectNotFoundException("Team", teamId));
        ActiveWeek week = weekRepository.findById(weekId)
                .orElseThrow(() -> new ObjectNotFoundException("ActiveWeek", weekId));

        List<AppUser> students = team.getStudents().stream()
                .sorted(Comparator.comparing(AppUser::getLastName))
                .toList();

        Set<Long> submitterIds = new HashSet<>();
        List<TeamWARReportResponse.StudentWAREntry> entries = new ArrayList<>();

        for (AppUser student : students) {
            List<WARActivity> activities = warRepository.findByStudentIdAndWeekId(student.getId(), weekId);
            if (!activities.isEmpty()) submitterIds.add(student.getId());

            List<TeamWARReportResponse.ActivityRow> rows = activities.stream()
                    .map(a -> new TeamWARReportResponse.ActivityRow(
                            a.getId(), a.getCategory(), a.getActivity(),
                            a.getDescription(), a.getPlannedHours(), a.getActualHours(), a.getStatus()))
                    .toList();

            entries.add(new TeamWARReportResponse.StudentWAREntry(
                    student.getId(),
                    student.getFirstName() + " " + student.getLastName(),
                    rows
            ));
        }

        List<String> nonSubmitters = students.stream()
                .filter(s -> !submitterIds.contains(s.getId()))
                .map(s -> s.getFirstName() + " " + s.getLastName())
                .toList();

        return new TeamWARReportResponse(
                teamId, team.getName(),
                weekId, week.getWeekStart().toString(),
                entries, nonSubmitters
        );
    }

    // ── UC-33: Student peer eval report over date range ───────────────────────

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Transactional(readOnly = true)
    public StudentPeerRangeResponse studentPeerReport(Long studentId, LocalDate start, LocalDate end) {
        AppUser student = userRepository.findById(studentId)
                .orElseThrow(() -> new ObjectNotFoundException("User", studentId));

        List<PeerEvaluation> evals = evalRepository.findByEvaluateeId(studentId).stream()
                .filter(PeerEvaluation::isSubmitted)
                .filter(e -> {
                    LocalDate ws = e.getWeek().getWeekStart();
                    return !ws.isBefore(start) && !ws.isAfter(end);
                })
                .sorted(Comparator.comparing(e -> e.getWeek().getWeekStart()))
                .toList();

        // Group by week
        Map<Long, List<PeerEvaluation>> byWeek = evals.stream()
                .collect(Collectors.groupingBy(e -> e.getWeek().getId(), LinkedHashMap::new, Collectors.toList()));

        List<StudentPeerRangeResponse.WeekEntry> weekEntries = byWeek.entrySet().stream()
                .map(entry -> {
                    ActiveWeek week = entry.getValue().get(0).getWeek();
                    List<PeerEvaluation> weekEvals = entry.getValue();
                    BigDecimal grade = computeGrade(weekEvals);
                    List<StudentPeerSummary.EvaluatorDetail> details = weekEvals.stream()
                            .map(this::toEvaluatorDetail)
                            .toList();
                    return new StudentPeerRangeResponse.WeekEntry(
                            week.getId(), week.getWeekStart().toString(), grade, details);
                })
                .toList();

        return new StudentPeerRangeResponse(
                studentId,
                student.getFirstName() + " " + student.getLastName(),
                weekEntries
        );
    }

    // ── UC-34: Student WAR report over date range ─────────────────────────────

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Transactional(readOnly = true)
    public StudentWARRangeResponse studentWARReport(Long studentId, LocalDate start, LocalDate end) {
        AppUser student = userRepository.findById(studentId)
                .orElseThrow(() -> new ObjectNotFoundException("User", studentId));

        List<WARActivity> activities = warRepository.findByStudentId(studentId).stream()
                .filter(a -> {
                    LocalDate ws = a.getWeek().getWeekStart();
                    return !ws.isBefore(start) && !ws.isAfter(end);
                })
                .sorted(Comparator.comparing(a -> a.getWeek().getWeekStart()))
                .toList();

        // Group by week
        Map<Long, List<WARActivity>> byWeek = activities.stream()
                .collect(Collectors.groupingBy(a -> a.getWeek().getId(), LinkedHashMap::new, Collectors.toList()));

        List<StudentWARRangeResponse.WeekEntry> weekEntries = byWeek.entrySet().stream()
                .map(entry -> {
                    ActiveWeek week = entry.getValue().get(0).getWeek();
                    List<TeamWARReportResponse.ActivityRow> rows = entry.getValue().stream()
                            .map(a -> new TeamWARReportResponse.ActivityRow(
                                    a.getId(), a.getCategory(), a.getActivity(),
                                    a.getDescription(), a.getPlannedHours(), a.getActualHours(), a.getStatus()))
                            .toList();
                    return new StudentWARRangeResponse.WeekEntry(
                            week.getId(), week.getWeekStart().toString(), rows);
                })
                .toList();

        return new StudentWARRangeResponse(
                studentId,
                student.getFirstName() + " " + student.getLastName(),
                weekEntries
        );
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private StudentPeerSummary buildStudentSummary(AppUser student, List<PeerEvaluation> allEvals) {
        List<PeerEvaluation> received = allEvals.stream()
                .filter(e -> e.getEvaluatee().getId().equals(student.getId()))
                .toList();

        BigDecimal grade = computeGrade(received);
        boolean submitted = !received.isEmpty();

        List<StudentPeerSummary.EvaluatorDetail> details = received.stream()
                .map(this::toEvaluatorDetail)
                .toList();

        return new StudentPeerSummary(
                student.getId(),
                student.getFirstName() + " " + student.getLastName(),
                grade, submitted, details
        );
    }

    private StudentPeerSummary.EvaluatorDetail toEvaluatorDetail(PeerEvaluation e) {
        List<StudentPeerSummary.ScoreDetail> scores = e.getScores().stream()
                .map(s -> new StudentPeerSummary.ScoreDetail(s.getCriterion().getName(), s.getScore()))
                .toList();
        return new StudentPeerSummary.EvaluatorDetail(
                e.getEvaluator().getId(),
                e.getEvaluator().getFirstName() + " " + e.getEvaluator().getLastName(),
                scores,
                e.getPublicComments(),
                e.getPrivateComments()
        );
    }

    /** Grade = average of per-evaluator totals (UC-29/31/33 algorithm). */
    private BigDecimal computeGrade(List<PeerEvaluation> evals) {
        if (evals.isEmpty()) return BigDecimal.ZERO;
        List<BigDecimal> totals = evals.stream()
                .map(e -> e.getScores().stream()
                        .map(s -> BigDecimal.valueOf(s.getScore()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .toList();
        return totals.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(totals.size()), 2, RoundingMode.HALF_UP);
    }
}
