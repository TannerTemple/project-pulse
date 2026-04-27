package edu.tcu.cs.projectpulse.activeweek;

import edu.tcu.cs.projectpulse.activeweek.dto.ActiveWeekSetupRequest;
import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.section.Section;
import edu.tcu.cs.projectpulse.section.SectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ActiveWeekServiceTest {

    @Mock ActiveWeekRepository activeWeekRepository;
    @Mock SectionService sectionService;
    @InjectMocks ActiveWeekService activeWeekService;

    private Section section;

    @BeforeEach
    void setUp() {
        section = new Section();
        section.setId(1L);
        section.setName("2024-2025");
        // Monday 2024-09-02 → Monday 2024-09-23 gives 4 weeks
        section.setStartDate(LocalDate.of(2024, 9, 2));
        section.setEndDate(LocalDate.of(2024, 9, 22));
    }

    // ── findBySectionId ───────────────────────────────────────────────────────

    @Test
    void findBySectionId_delegatesToRepository() {
        ActiveWeek week = new ActiveWeek();
        week.setId(1L);
        week.setWeekStart(LocalDate.of(2024, 9, 2));
        given(activeWeekRepository.findBySectionIdOrderByWeekStartAsc(1L)).willReturn(List.of(week));

        List<ActiveWeek> result = activeWeekService.findBySectionId(1L);

        assertThat(result).hasSize(1);
        then(activeWeekRepository).should().findBySectionIdOrderByWeekStartAsc(1L);
    }

    // ── setupWeeks ────────────────────────────────────────────────────────────

    @Test
    void setupWeeks_generatesWeeksForSectionRange() {
        given(sectionService.getSectionOrThrow(1L)).willReturn(section);
        given(activeWeekRepository.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));

        List<ActiveWeek> result = activeWeekService.setupWeeks(1L,
                new ActiveWeekSetupRequest(Collections.emptySet()));

        // 2024-09-02 through 2024-09-22 → 3 Mondays: Sep 2, Sep 9, Sep 16
        assertThat(result).hasSize(3);
        assertThat(result).allMatch(ActiveWeek::isActive);
    }

    @Test
    void setupWeeks_marksSpecifiedWeeksInactive() {
        given(sectionService.getSectionOrThrow(1L)).willReturn(section);
        given(activeWeekRepository.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));

        LocalDate inactive = LocalDate.of(2024, 9, 9);
        List<ActiveWeek> result = activeWeekService.setupWeeks(1L,
                new ActiveWeekSetupRequest(Set.of(inactive)));

        long inactiveCount = result.stream().filter(w -> !w.isActive()).count();
        assertThat(inactiveCount).isEqualTo(1);
        assertThat(result.stream().filter(w -> !w.isActive()).findFirst())
                .isPresent()
                .hasValueSatisfying(w -> assertThat(w.getWeekStart()).isEqualTo(inactive));
    }

    @Test
    void setupWeeks_deletesExistingWeeksBeforeRegeneration() {
        given(sectionService.getSectionOrThrow(1L)).willReturn(section);
        given(activeWeekRepository.saveAll(anyList())).willAnswer(inv -> inv.getArgument(0));

        activeWeekService.setupWeeks(1L, new ActiveWeekSetupRequest(Collections.emptySet()));

        then(activeWeekRepository).should().deleteBySectionId(1L);
        then(activeWeekRepository).should().flush();
    }

    @Test
    void setupWeeks_givenInvalidSectionId_throwsObjectNotFoundException() {
        given(sectionService.getSectionOrThrow(99L))
                .willThrow(new ObjectNotFoundException("Section", 99L));

        assertThatThrownBy(() -> activeWeekService.setupWeeks(99L,
                new ActiveWeekSetupRequest(Collections.emptySet())))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }
}
