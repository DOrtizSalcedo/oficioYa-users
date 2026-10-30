package edu.eci.dosw.oficioya_users.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.dosw.oficioya_users.dto.ApiErrorDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/** Respuestas 401 y 403 en el mismo formato JSON que el resto de errores de la API. */
@Component
@RequiredArgsConstructor
public class SecurityErrorHandler {

    private final ObjectMapper objectMapper;

    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, ex) ->
                write(response, HttpStatus.UNAUTHORIZED, "Token ausente, inválido o expirado");
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) ->
                write(response, HttpStatus.FORBIDDEN, "No tiene permisos para esta operación");
    }

    private void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                new ApiErrorDTO(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, null));
    }
}