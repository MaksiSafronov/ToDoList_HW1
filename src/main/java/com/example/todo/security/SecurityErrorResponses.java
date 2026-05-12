package com.example.todo.security;

import com.example.todo.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.Instant;

public final class SecurityErrorResponses {

    private SecurityErrorResponses() {
    }

    public static void writeJson(
            HttpServletRequest request,
            HttpServletResponse response,
            ObjectMapper objectMapper,
            HttpStatus status,
            String message) throws IOException {
        ErrorResponse body = new ErrorResponse();
        body.setTimestamp(Instant.now());
        body.setStatus(status.value());
        body.setError(status.getReasonPhrase());
        body.setMessage(message);
        body.setPath(request.getRequestURI());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
