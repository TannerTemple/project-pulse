package edu.tcu.cs.projectpulse.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Semicolon-separated list of email addresses as defined in UC-11 and UC-18.
 * Example: "a@tcu.edu; b@tcu.edu; c@tcu.edu"
 */
public record InviteRequest(
        @NotBlank String emails,
        String customMessage
) {}
