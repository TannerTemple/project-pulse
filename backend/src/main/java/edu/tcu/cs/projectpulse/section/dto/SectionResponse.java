package edu.tcu.cs.projectpulse.section.dto;

import edu.tcu.cs.projectpulse.rubric.dto.RubricResponse;
import edu.tcu.cs.projectpulse.section.Section;

import java.time.LocalDate;
import java.util.List;

public record SectionResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        RubricResponse rubric,
        List<String> teamNames
) {
    public static SectionResponse from(Section s) {
        List<String> teams = s.getTeams().stream().map(t -> t.getName()).sorted().toList();
        RubricResponse rubric = s.getRubric() != null ? RubricResponse.from(s.getRubric()) : null;
        return new SectionResponse(s.getId(), s.getName(), s.getStartDate(), s.getEndDate(), rubric, teams);
    }
}
