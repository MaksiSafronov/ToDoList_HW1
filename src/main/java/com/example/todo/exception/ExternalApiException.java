package com.example.todo.exception;

import org.springframework.http.HttpStatusCode;

public class ExternalApiException extends RuntimeException {

    private final HttpStatusCode status;

    public ExternalApiException(HttpStatusCode status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatusCode getStatus() {
        return status;
    }
}
