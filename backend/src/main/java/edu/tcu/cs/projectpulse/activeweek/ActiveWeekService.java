package edu.tcu.cs.projectpulse.activeweek;

import edu.tcu.cs.projectpulse.activeweek.dto.ActiveWeekSetupRequest;
import edu.tcu.cs.projectpulse.section.Section;
import edu.tcu.cs.projectpulse.section.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ActiveWeekService {

    private final ActiveWeekRepository activeWeekRepository;
    private final SectionService sectionService;

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR','STUDENT')")
    @Transactional(readOnly = true)
    public List<ActiveWeek> findBySectionId(Long sectionId) {
        return activeWeekRepository.findBySectionIdOrderByWeekStartAsc(sectionId);
    }

    /**
     * Generates all Monday-aligned weeks between section start and end dates,
     * then marks the ones in {@code request.inactiveWeekStarts()} as inactive (UC-6).
     * Replaces any existing week configuration for this section.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public List<ActiveWeek> setupWeeks(Long sectionId, ActiveWeekSetupRequest request) {
        Section section = sectionService.getSectionOrThrow(sectionId);

        // Remove existing weeks first
        activeWeekRepository.deleteAll(
                activeWeekRepository.findBySectionIdOrderByWeekStartAsc(sectionId));

        Set<LocalDate> inactives = request.inactiveWeekStarts();
        List<ActiveWeek> weeks = new ArrayList<>();

        // Walk week by week (Monday = week start) from section start to end
        LocalDate cursor = toMonday(section.getStartDate());
        while (!cursor.isAfter(section.getEndDate())) {
            ActiveWeek week = new ActiveWeek();
            week.setWeekStart(cursor);
            week.setSection(section);
            week.setActive(!inactives.contains(cursor));
            weeks.add(week);
            cursor = cursor.plusWeeks(1);
        }

        return activeWeekRepository.saveAll(weeks);
    }

    // ── private ───────────────────────────────────────────────────────────────

    private LocalDate toMonday(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }
}
