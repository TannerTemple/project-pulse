package edu.tcu.cs.projectpulse.auth.dto;

import edu.tcu.cs.projectpulse.user.UserRole;

public record LoginResponse(
        String token,
        Long userId,
        String firstName,
        String lastName,
        UserRole role
) {}
