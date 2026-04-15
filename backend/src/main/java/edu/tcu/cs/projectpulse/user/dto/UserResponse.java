package edu.tcu.cs.projectpulse.user.dto;

import edu.tcu.cs.projectpulse.user.AppUser;
import edu.tcu.cs.projectpulse.user.UserRole;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String middleInitial,
        String email,
        UserRole role,
        boolean active,
        boolean registrationComplete,
        Long sectionId,
        String sectionName,
        Long teamId,
        String teamName
) {
    public static UserResponse from(AppUser u) {
        return new UserResponse(
                u.getId(),
                u.getFirstName(),
                u.getLastName(),
                u.getMiddleInitial(),
                u.getEmail(),
                u.getRole(),
                u.isActive(),
                u.isRegistrationComplete(),
                u.getSection() != null ? u.getSection().getId() : null,
                u.getSection() != null ? u.getSection().getName() : null,
                u.getTeam() != null ? u.getTeam().getId() : null,
                u.getTeam() != null ? u.getTeam().getName() : null
        );
    }
}
