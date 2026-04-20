package edu.tcu.cs.projectpulse.war;

import edu.tcu.cs.projectpulse.activeweek.ActiveWeek;
import edu.tcu.cs.projectpulse.activeweek.ActiveWeekRepository;
import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.user.AppUser;
import edu.tcu.cs.projectpulse.user.UserRepository;
import edu.tcu.cs.projectpulse.war.dto.WARActivityRequest;
import edu.tcu.cs.projectpulse.war.dto.WARActivityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WARActivityService {

    private final WARActivityRepository warRepository;
    private final ActiveWeekRepository weekRepository;
    private final UserRepository userRepository;

    /**
     * Returns all WAR activities for the current student in the given week (UC-27).
     * Students can only see their own activities.
     */
    @PreAuthorize("hasRole('STUDENT')")
    @Transactional(readOnly = true)
    public List<WARActivityResponse> findMyActivities(Long weekId) {
        AppUser student = currentUser();
        return warRepository.findByStudentIdAndWeekId(student.getId(), weekId)
                .stream().map(WARActivityResponse::from).toList();
    }

    /**
     * Returns all WAR activities for a student (used by instructors/admin for reports).
     */
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @Transactional(readOnly = true)
    public List<WARActivityResponse> findByStudent(Long studentId, Long weekId) {
        List<WARActivity> results = weekId != null
                ? warRepository.findByStudentIdAndWeekId(studentId, weekId)
                : warRepository.findByStudentId(studentId);
        return results.stream().map(WARActivityResponse::from).toList();
    }

    /** Create a new WAR activity for the current student (UC-27). */
    @PreAuthorize("hasRole('STUDENT')")
    @Transactional
    public WARActivityResponse create(WARActivityRequest request) {
        AppUser student = currentUser();
        ActiveWeek week = getWeekOrThrow(request.weekId());
        validateNotFutureWeek(week);

        WARActivity activity = new WARActivity();
        mapFields(activity, request, student, week);
        return WARActivityResponse.from(warRepository.save(activity));
    }

    /** Update an existing WAR activity (UC-27). Students can only edit their own. */
    @PreAuthorize("hasRole('STUDENT')")
    @Transactional
    public WARActivityResponse update(Long id, WARActivityRequest request) {
        AppUser student = currentUser();
        WARActivity activity = getActivityOrThrow(id);

        if (!activity.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("You can only edit your own WAR activities.");
        }

        ActiveWeek week = getWeekOrThrow(request.weekId());
        validateNotFutureWeek(week);
        mapFields(activity, request, student, week);
        return WARActivityResponse.from(warRepository.save(activity));
    }

    /** Delete a WAR activity (UC-27). Students can only delete their own. */
    @PreAuthorize("hasRole('STUDENT')")
    @Transactional
    public void delete(Long id) {
        AppUser student = currentUser();
        WARActivity activity = getActivityOrThrow(id);

        if (!activity.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("You can only delete your own WAR activities.");
        }
        warRepository.delete(activity);
    }

    // ── private ───────────────────────────────────────────────────────────────

    private void mapFields(WARActivity a, WARActivityRequest req, AppUser student, ActiveWeek week) {
        a.setCategory(req.category());
        a.setActivity(req.activity());
        a.setDescription(req.description());
        a.setPlannedHours(req.plannedHours());
        a.setActualHours(req.actualHours());
        a.setStatus(req.status());
        a.setStudent(student);
        a.setWeek(week);
    }

    private WARActivity getActivityOrThrow(Long id) {
        return warRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("WARActivity", id));
    }

    private ActiveWeek getWeekOrThrow(Long weekId) {
        return weekRepository.findById(weekId)
                .orElseThrow(() -> new ObjectNotFoundException("ActiveWeek", weekId));
    }

    /** BR-2 variant: students cannot select a future week for WAR submission. */
    private void validateNotFutureWeek(ActiveWeek week) {
        if (week.getWeekStart().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot submit WAR activities for a future week.");
        }
    }

    private AppUser currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ObjectNotFoundException("Current user not found."));
    }
}
