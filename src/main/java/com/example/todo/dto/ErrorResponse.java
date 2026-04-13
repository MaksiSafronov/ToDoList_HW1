package com.example.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(name = "ErrorResponse", description = "Единый формат ошибки API")
public class ErrorResponse {

    @Schema(description = "Время ошибки (UTC)")
    private Instant timestamp;

    @Schema(description = "HTTP-код", example = "400")
    private int status;

    @Schema(description = "Краткое описание статуса", example = "Bad Request")
    private String error;

    @Schema(description = "Сообщение для клиента")
    private String message;

    @Schema(description = "Путь запроса", example = "/api/tasks/1")
    private String path;

    @Schema(description = "Дополнительные поля (например, ошибки валидации полей)")
    private Map<String, Object> details;

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}
