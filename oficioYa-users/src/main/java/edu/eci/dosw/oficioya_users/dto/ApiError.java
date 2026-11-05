package edu.eci.dosw.oficioya_users.dto;

import java.time.LocalDateTime;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String route) {

    public static ApiError of(int status, String error, String message, String route) {
        return new ApiError(LocalDateTime.now(), status, error, message, route);
    }
}
