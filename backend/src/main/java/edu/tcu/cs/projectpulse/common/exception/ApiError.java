package edu.tcu.cs.projectpulse.common.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        List<String> messages
) {
    public static ApiError of(int status, String error, List<String> messages) {
        return new ApiError(LocalDateTime.now(), status, error, messages);
    }

    public static ApiError of(int status, String error, String message) {
        return of(status, error, List.of(message));
    }
}
