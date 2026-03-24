package com.example.todo.config;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.time.Instant;
import java.util.UUID;

/**
 * Request-scoped бин, создающийся заново для каждого HTTP-запроса.
 * Хранит уникальный requestId и время начала обработки запроса.
 */
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

