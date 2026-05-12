package com.example.todo.external;

import com.example.todo.dto.external.ExternalTaskCreateRequest;
import com.example.todo.dto.external.ExternalTaskResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/external/v1")
public class ExternalApiController {

    private final AtomicLong idSeq = new AtomicLong(0L);
    private final Map<Long, ExternalTaskResponse> tasks = new ConcurrentHashMap<>();

    @PostMapping("/tasks")
    public ResponseEntity<ExternalTaskResponse> createTask(@Valid @RequestBody ExternalTaskCreateRequest request) {
        long id = idSeq.incrementAndGet();
        ExternalTaskResponse created = toTask(id, request);
        tasks.put(id, created);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<?> getTask(@PathVariable long id) {
        ExternalTaskResponse found = tasks.get(id);
        if (found == null) {
            return taskNotFound(id);
        }
        return ResponseEntity.ok(found);
    }

    @GetMapping("/tasks")
    public List<ExternalTaskResponse> listTasks(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Integer limit) {
        return tasks.values().stream()
                .filter(task -> completed == null || task.isCompleted() == completed)
                .sorted(Comparator.comparing(ExternalTaskResponse::getId))
                .limit(limit != null && limit > 0 ? limit : Long.MAX_VALUE)
                .toList();
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<?> updateTask(@PathVariable long id, @Valid @RequestBody ExternalTaskCreateRequest request) {
        ExternalTaskResponse existing = tasks.get(id);
        if (existing == null) {
            return taskNotFound(id);
        }
        ExternalTaskResponse updated = toTask(id, request);
        tasks.put(id, updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable long id) {
        ExternalTaskResponse removed = tasks.remove(id);
        if (removed == null) {
            return taskNotFound(id);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unstable")
    public ResponseEntity<?> unstable(@RequestParam(name = "mode", defaultValue = "500") String mode)
            throws InterruptedException {
        return switch (mode) {
            case "timeout" -> {
                Thread.sleep(7000);
                ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                        HttpStatus.GATEWAY_TIMEOUT,
                        "Simulated timeout after client deadline"
                );
                pd.setTitle("Upstream Timeout");
                yield ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(pd);
            }
            case "500" -> {
                ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Simulated upstream internal error"
                );
                pd.setTitle("Upstream Failure");
                yield ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
            }
            case "429" -> {
                ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Rate limit exceeded in simulated upstream"
                );
                pd.setTitle("Too Many Requests");
                yield ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .header(HttpHeaders.RETRY_AFTER, "3")
                        .body(pd);
            }
            case "html" -> ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.TEXT_HTML)
                    .body("<html><body><h1>502 Bad Gateway</h1><p>HTML response simulation</p></body></html>");
            default -> {
                ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "Unsupported mode. Use timeout|500|429|html"
                );
                pd.setTitle("Invalid unstable mode");
                yield ResponseEntity.badRequest().body(pd);
            }
        };
    }

    private static ExternalTaskResponse toTask(long id, ExternalTaskCreateRequest request) {
        ExternalTaskResponse task = new ExternalTaskResponse();
        task.setId(id);
        task.setTitle(request.getTitle());
        task.setCompleted(request.isCompleted());
        return task;
    }

    private ResponseEntity<ProblemDetail> taskNotFound(long id) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Task with id " + id + " not found"
        );
        pd.setTitle("Task Not Found");
        pd.setType(URI.create("about:blank"));
        pd.setProperty("taskId", id);
        pd.setProperty("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }
}
