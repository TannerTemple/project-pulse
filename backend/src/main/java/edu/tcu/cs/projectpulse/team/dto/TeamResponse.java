package edu.tcu.cs.projectpulse.team.dto;

import edu.tcu.cs.projectpulse.team.Team;
import edu.tcu.cs.projectpulse.user.AppUser;

import java.util.List;

public record TeamResponse(
        Long id,
        String name,
        String description,
        String websiteUrl,
        Long sectionId,
        String sectionName,
        List<MemberSummary> students,
        List<MemberSummary> instructors
) {
    public record MemberSummary(Long id, String firstName, String lastName, String email) {}

    public static TeamResponse from(Team t) {
        List<MemberSummary> students = t.getStudents().stream()
                .map(TeamResponse::toSummary).toList();
        List<MemberSummary> instructors = t.getInstructors().stream()
                .map(TeamResponse::toSummary).toList();
        return new TeamResponse(
                t.getId(), t.getName(), t.getDescription(), t.getWebsiteUrl(),
                t.getSection().getId(), t.getSection().getName(),
                students, instructors);
    }

    private static MemberSummary toSummary(AppUser u) {
        return new MemberSummary(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail());
    }
}
