package edu.tcu.cs.projectpulse.section;

import edu.tcu.cs.projectpulse.common.exception.ObjectNotFoundException;
import edu.tcu.cs.projectpulse.rubric.Rubric;
import edu.tcu.cs.projectpulse.rubric.RubricRepository;
import edu.tcu.cs.projectpulse.section.dto.SectionRequest;
import edu.tcu.cs.projectpulse.section.dto.SectionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final RubricRepository rubricRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<SectionResponse> findAll(String name) {
        List<Section> results = (name != null && !name.isBlank())
                ? sectionRepository.findByNameContainingIgnoreCase(name)
                : sectionRepository.findAll();
        return results.stream()
                .sorted((a, b) -> b.getName().compareTo(a.getName()))
                .map(SectionResponse::from)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public SectionResponse findById(Long id) {
        return SectionResponse.from(getSectionOrThrow(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public SectionResponse create(SectionRequest request) {
        if (sectionRepository.existsByName(request.name())) {
            throw new IllegalStateException("Section \"" + request.name() + "\" already exists.");
        }
        Rubric rubric = rubricRepository.findById(request.rubricId())
                .orElseThrow(() -> new ObjectNotFoundException("Rubric", request.rubricId()));

        Section section = new Section();
        section.setName(request.name());
        section.setStartDate(request.startDate());
        section.setEndDate(request.endDate());
        section.setRubric(rubric);

        return SectionResponse.from(sectionRepository.save(section));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public SectionResponse update(Long id, SectionRequest request) {
        Section section = getSectionOrThrow(id);

        if (!section.getName().equals(request.name())
                && sectionRepository.existsByName(request.name())) {
            throw new IllegalStateException("Section \"" + request.name() + "\" already exists.");
        }

        Rubric rubric = rubricRepository.findById(request.rubricId())
                .orElseThrow(() -> new ObjectNotFoundException("Rubric", request.rubricId()));

        section.setName(request.name());
        section.setStartDate(request.startDate());
        section.setEndDate(request.endDate());
        section.setRubric(rubric);

        return SectionResponse.from(sectionRepository.save(section));
    }

    // ── private ───────────────────────────────────────────────────────────────

    public Section getSectionOrThrow(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Section", id));
    }
}
