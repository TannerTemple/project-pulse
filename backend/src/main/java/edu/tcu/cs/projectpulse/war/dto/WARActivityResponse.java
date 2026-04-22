package edu.tcu.cs.projectpulse.war.dto;

import edu.tcu.cs.projectpulse.war.ActivityCategory;
import edu.tcu.cs.projectpulse.war.ActivityStatus;
import edu.tcu.cs.projectpulse.war.WARActivity;

import java.time.LocalDate;

public record WARActivityResponse(
        Long id,
        ActivityCategory category,
        String activity,
        String description,
        double plannedHours,
        Double actualHours,
        ActivityStatus status,
        Long weekId,
        LocalDate weekStart,
        Long studentId,
        String studentName
) {
    public static WARActivityResponse from(WARActivity w) {
        return new WARActivityResponse(
                w.getId(),
                w.getCategory(),
                w.getActivity(),
                w.getDescription(),
                w.getPlannedHours(),
                w.getActualHours(),
                w.getStatus(),
                w.getWeek().getId(),
                w.getWeek().getWeekStart(),
                w.getStudent().getId(),
                w.getStudent().getFirstName() + " " + w.getStudent().getLastName()
        );
    }
}
