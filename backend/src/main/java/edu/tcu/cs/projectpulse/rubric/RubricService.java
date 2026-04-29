package edu.tcu.cs.projectpulse.rubric;

import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.rubric.dto.CriterionDto;
import edu.tcu.cs.projectpulse.rubric.dto.RubricRequest;
import edu.tcu.cs.projectpulse.rubric.dto.RubricResponse;
import edu.tcu.cs.projectpulse.section.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RubricService {

    private final RubricRepository rubricRepository;
    private final CriterionRepository criterionRepository;
    private final SectionRepository sectionRepository;

    @Transactional(readOnly = true)
    public List<RubricResponse> findAll() {
        return rubricRepository.findAll().stream().map(RubricResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RubricResponse findById(Long id) {
        return RubricResponse.from(getRubricOrThrow(id));
    }

    @Transactional
    public RubricResponse create(RubricRequest request) {
        if (rubricRepository.existsByName(request.name())) {
            throw new IllegalStateException("A rubric named \"" + request.name() + "\" already exists.");
        }
        Rubric rubric = new Rubric();
        rubric.setName(request.name());
        mapCriteria(rubric, request.criteria());
        return RubricResponse.from(rubricRepository.save(rubric));
    }

    @Transactional
    public RubricResponse update(Long id, RubricRequest request) {
        Rubric rubric = getRubricOrThrow(id);
        if (sectionRepository.existsByRubricId(id)) {
            throw new IllegalStateException(
                    "This rubric is assigned to one or more sections and cannot be modified. Create a new rubric instead.");
        }
        if (!rubric.getName().equals(request.name()) && rubricRepository.existsByName(request.name())) {
            throw new IllegalStateException("A rubric named \"" + request.name() + "\" already exists.");
        }
        rubric.setName(request.name());
        mapCriteria(rubric, request.criteria());
        return RubricResponse.from(rubricRepository.save(rubric));
    }

    // ── private ───────────────────────────────────────────────────────────────

    private Rubric getRubricOrThrow(Long id) {
        return rubricRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Rubric", id));
    }

    private void mapCriteria(Rubric rubric, List<CriterionDto> dtos) {
        rubric.getCriteria().clear();
        for (int i = 0; i < dtos.size(); i++) {
            CriterionDto dto = dtos.get(i);
            Criterion c = new Criterion();
            c.setName(dto.name());
            c.setDescription(dto.description());
            c.setMaxScore(dto.maxScore());
            c.setOrderIndex(i);
            c.setRubric(rubric);
            rubric.getCriteria().add(c);
        }
    }
}
