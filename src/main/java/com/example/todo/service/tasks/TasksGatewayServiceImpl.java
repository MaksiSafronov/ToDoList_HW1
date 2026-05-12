package com.example.todo.service.tasks;

import com.example.todo.client.ExternalTasksClient;
import com.example.todo.dto.external.ExternalTaskCreateRequest;
import com.example.todo.dto.external.ExternalTaskResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TasksGatewayServiceImpl implements TasksGatewayService {

    private static final Logger log = LoggerFactory.getLogger(TasksGatewayServiceImpl.class);

    private final ExternalTasksClient externalTasksClient;

    public TasksGatewayServiceImpl(ExternalTasksClient externalTasksClient) {
        this.externalTasksClient = externalTasksClient;
    }

    @Override
    @RateLimiter(name = "externalApi")
    @CircuitBreaker(name = "externalApi", fallbackMethod = "createTaskFallback")
    public CreatedTask createTask(ExternalTaskCreateRequest request) {
        return externalTasksClient.createTask(request);
    }

    @Override
    @RateLimiter(name = "externalApi")
    @CircuitBreaker(name = "externalApi", fallbackMethod = "getTaskFallback")
    public ExternalTaskResponse getTask(long id) {
        return externalTasksClient.getTask(id);
    }

    @Override
    @RateLimiter(name = "externalApi")
    @CircuitBreaker(name = "externalApi", fallbackMethod = "listTasksFallback")
    public List<ExternalTaskResponse> listTasks(Boolean completed, Integer limit) {
        return externalTasksClient.listTasks(completed, limit);
    }

    @Override
    @RateLimiter(name = "externalApi")
    @CircuitBreaker(name = "externalApi", fallbackMethod = "deleteTaskFallback")
    public void deleteTask(long id) {
        externalTasksClient.deleteTask(id);
    }

    @SuppressWarnings("unused")
    public CreatedTask createTaskFallback(ExternalTaskCreateRequest request, Throwable throwable) {
        log.warn("Fallback for createTask triggered: {}", throwable.toString());
        ExternalTaskResponse fallback = new ExternalTaskResponse();
        fallback.setId(-1L);
        fallback.setTitle(request.getTitle() + " (fallback)");
        fallback.setCompleted(request.isCompleted());
        return new CreatedTask(fallback, null);
    }

    @SuppressWarnings("unused")
    public ExternalTaskResponse getTaskFallback(long id, Throwable throwable) {
        log.warn("Fallback for getTask({}) triggered: {}", id, throwable.toString());
        ExternalTaskResponse fallback = new ExternalTaskResponse();
        fallback.setId(id);
        fallback.setTitle("Task temporarily unavailable (fallback)");
        fallback.setCompleted(false);
        return fallback;
    }

    @SuppressWarnings("unused")
    public List<ExternalTaskResponse> listTasksFallback(Boolean completed, Integer limit, Throwable throwable) {
        log.warn("Fallback for listTasks(completed={}, limit={}) triggered: {}", completed, limit, throwable.toString());
        return List.of();
    }

    @SuppressWarnings("unused")
    public void deleteTaskFallback(long id, Throwable throwable) {
        log.warn("Fallback for deleteTask({}) triggered: {}", id, throwable.toString());
    }
}
