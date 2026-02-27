package com.example.todo.config;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.time.Instant;
import java.util.UUID;

@Component
@RequestScope
public class RequestScopedBean {

    private final String requestId;
    private final Instant startTime;

    public RequestScopedBean() {
        this.requestId = UUID.randomUUID().toString();
        this.startTime = Instant.now();
    }

    public String getRequestId() {
        return requestId;
    }

    public Instant getStartTime() {
        return startTime;
    }
}

