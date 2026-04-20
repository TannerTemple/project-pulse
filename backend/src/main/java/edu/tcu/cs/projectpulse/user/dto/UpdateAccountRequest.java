package edu.tcu.cs.projectpulse.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAccountRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String middleInitial,
        @Size(min = 8) String newPassword,
        String confirmPassword
) {}
