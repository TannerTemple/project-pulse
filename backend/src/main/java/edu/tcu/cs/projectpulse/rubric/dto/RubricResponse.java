package edu.tcu.cs.projectpulse.rubric.dto;

import edu.tcu.cs.projectpulse.rubric.Criterion;
import edu.tcu.cs.projectpulse.rubric.Rubric;

import java.util.List;

public record RubricResponse(
        Long id,
        String name,
        List<CriterionDto> criteria
) {
    public static RubricResponse from(Rubric r) {
        List<CriterionDto> dtos = r.getCriteria().stream()
                .map(c -> new CriterionDto(c.getId(), c.getName(), c.getDescription(),
                        c.getMaxScore(), c.getOrderIndex()))
                .toList();
        return new RubricResponse(r.getId(), r.getName(), dtos);
    }
}
