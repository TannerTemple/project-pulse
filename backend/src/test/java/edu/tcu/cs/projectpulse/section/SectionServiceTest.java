package edu.tcu.cs.projectpulse.section;

import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.rubric.Rubric;
import edu.tcu.cs.projectpulse.rubric.RubricRepository;
import edu.tcu.cs.projectpulse.section.dto.SectionRequest;
import edu.tcu.cs.projectpulse.section.dto.SectionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class SectionServiceTest {

    @Mock SectionRepository sectionRepository;
    @Mock RubricRepository  rubricRepository;
    @InjectMocks SectionService sectionService;

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_givenNoFilter_returnsAllSections() {
        Section s = sectionWithId(1L, "2024-2025");
        given(sectionRepository.findAll()).willReturn(List.of(s));

        List<SectionResponse> result = sectionService.findAll(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("2024-2025");
    }

    @Test
    void findAll_givenNameFilter_delegatesToRepository() {
        given(sectionRepository.findByNameContainingIgnoreCase("2024")).willReturn(List.of());

        List<SectionResponse> result = sectionService.findAll("2024");

        assertThat(result).isEmpty();
        then(sectionRepository).should().findByNameContainingIgnoreCase("2024");
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findById_givenValidId_returnsSection() {
        Section s = sectionWithId(1L, "2024-2025");
        given(sectionRepository.findById(1L)).willReturn(Optional.of(s));

        SectionResponse result = sectionService.findById(1L);

        assertThat(result.name()).isEqualTo("2024-2025");
    }

    @Test
    void findById_givenInvalidId_throwsObjectNotFoundException() {
        given(sectionRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.findById(99L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_givenValidRequest_savesAndReturnsSection() {
        Rubric rubric = rubricWithId(10L);
        given(sectionRepository.existsByName("2025-2026")).willReturn(false);
        given(rubricRepository.findById(10L)).willReturn(Optional.of(rubric));
        given(sectionRepository.save(any(Section.class))).willAnswer(inv -> {
            Section s = inv.getArgument(0);
            s.setId(2L);
            return s;
        });

        SectionRequest req = new SectionRequest(
                "2025-2026",
                LocalDate.of(2025, 8, 25),
                LocalDate.of(2026, 5, 10),
                10L
        );
        SectionResponse result = sectionService.create(req);

        assertThat(result.name()).isEqualTo("2025-2026");
        then(sectionRepository).should().save(any(Section.class));
    }

    @Test
    void create_givenDuplicateName_throwsIllegalStateException() {
        given(sectionRepository.existsByName("2024-2025")).willReturn(true);

        SectionRequest req = new SectionRequest("2024-2025",
                LocalDate.now(), LocalDate.now().plusYears(1), 1L);

        assertThatThrownBy(() -> sectionService.create(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("2024-2025");
    }

    @Test
    void create_givenInvalidRubricId_throwsObjectNotFoundException() {
        given(sectionRepository.existsByName("2025-2026")).willReturn(false);
        given(rubricRepository.findById(99L)).willReturn(Optional.empty());

        SectionRequest req = new SectionRequest("2025-2026",
                LocalDate.now(), LocalDate.now().plusYears(1), 99L);

        assertThatThrownBy(() -> sectionService.create(req))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Section sectionWithId(Long id, String name) {
        Section s = new Section();
        s.setId(id);
        s.setName(name);
        s.setStartDate(LocalDate.of(2024, 8, 26));
        s.setEndDate(LocalDate.of(2025, 5, 10));
        s.setTeams(new ArrayList<>());
        return s;
    }

    private Rubric rubricWithId(Long id) {
        Rubric r = new Rubric();
        r.setId(id);
        r.setName("Test Rubric");
        r.setCriteria(new ArrayList<>());
        return r;
    }
}
