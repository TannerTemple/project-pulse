package edu.tcu.cs.projectpulse.rubric;

import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.rubric.dto.CriterionDto;
import edu.tcu.cs.projectpulse.rubric.dto.RubricRequest;
import edu.tcu.cs.projectpulse.rubric.dto.RubricResponse;
import edu.tcu.cs.projectpulse.section.SectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class RubricServiceTest {

    @Mock RubricRepository rubricRepository;
    @Mock CriterionRepository criterionRepository;
    @Mock SectionRepository sectionRepository;
    @InjectMocks RubricService rubricService;

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsAllRubrics() {
        given(rubricRepository.findAll()).willReturn(List.of(rubricWithId(1L, "Rubric A")));

        List<RubricResponse> result = rubricService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Rubric A");
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findById_givenValidId_returnsRubric() {
        given(rubricRepository.findById(1L)).willReturn(Optional.of(rubricWithId(1L, "Rubric A")));

        RubricResponse result = rubricService.findById(1L);

        assertThat(result.name()).isEqualTo("Rubric A");
    }

    @Test
    void findById_givenInvalidId_throwsObjectNotFoundException() {
        given(rubricRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> rubricService.findById(99L))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_givenValidRequest_savesAndReturnsRubric() {
        given(rubricRepository.existsByName("Peer Eval v1")).willReturn(false);
        given(rubricRepository.save(any(Rubric.class))).willAnswer(inv -> {
            Rubric r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });

        RubricRequest req = new RubricRequest("Peer Eval v1", List.of(
                new CriterionDto(null, "Quality", "Code quality", new BigDecimal("10"), 0),
                new CriterionDto(null, "Initiative", "Takes initiative", new BigDecimal("10"), 1)
        ));

        RubricResponse result = rubricService.create(req);

        assertThat(result.name()).isEqualTo("Peer Eval v1");
        assertThat(result.criteria()).hasSize(2);
        then(rubricRepository).should().save(any(Rubric.class));
    }

    @Test
    void create_givenDuplicateName_throwsIllegalStateException() {
        given(rubricRepository.existsByName("Peer Eval v1")).willReturn(true);

        RubricRequest req = new RubricRequest("Peer Eval v1", List.of(
                new CriterionDto(null, "Quality", null, new BigDecimal("10"), 0)
        ));

        assertThatThrownBy(() -> rubricService.create(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Peer Eval v1");
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_givenRubricAssignedToSection_throwsIllegalStateException() {
        given(rubricRepository.findById(1L)).willReturn(Optional.of(rubricWithId(1L, "Rubric A")));
        given(sectionRepository.existsByRubricId(1L)).willReturn(true);

        RubricRequest req = new RubricRequest("Rubric A Updated", List.of(
                new CriterionDto(null, "Quality", null, new BigDecimal("10"), 0)
        ));

        assertThatThrownBy(() -> rubricService.update(1L, req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("assigned to one or more sections");
        then(rubricRepository).should(never()).save(any());
    }

    @Test
    void update_givenUnassignedRubric_savesAndReturns() {
        Rubric existing = rubricWithId(1L, "Rubric A");
        given(rubricRepository.findById(1L)).willReturn(Optional.of(existing));
        given(sectionRepository.existsByRubricId(1L)).willReturn(false);
        given(rubricRepository.existsByName("Rubric A Updated")).willReturn(false);
        given(rubricRepository.save(any(Rubric.class))).willAnswer(inv -> inv.getArgument(0));

        RubricRequest req = new RubricRequest("Rubric A Updated", List.of(
                new CriterionDto(null, "Quality", null, new BigDecimal("10"), 0)
        ));

        RubricResponse result = rubricService.update(1L, req);

        assertThat(result.name()).isEqualTo("Rubric A Updated");
        then(rubricRepository).should().save(any(Rubric.class));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Rubric rubricWithId(Long id, String name) {
        Rubric r = new Rubric();
        r.setId(id);
        r.setName(name);
        r.setCriteria(new ArrayList<>());
        return r;
    }
}
