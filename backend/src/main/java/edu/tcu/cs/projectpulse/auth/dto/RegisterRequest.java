package edu.tcu.cs.projectpulse.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        /** The UUID token from the invitation email (UC-25, UC-30). */
        @NotBlank String token,

        @NotBlank String firstName,
        @NotBlank String lastName,

        /** Optional — instructors supply this; ignored for students. */
        String middleInitial,

        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password,
        @NotBlank String confirmPassword
) {}
