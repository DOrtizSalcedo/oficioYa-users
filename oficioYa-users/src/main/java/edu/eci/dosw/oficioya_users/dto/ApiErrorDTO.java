package edu.eci.dosw.oficioya_users.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorDTO(LocalDateTime timestamp, int status, String error, String message,
                          Map<String, String> fieldErrors) {
}
