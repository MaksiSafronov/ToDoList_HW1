package com.example.todo.api;

import com.example.todo.dto.external.ExternalTaskCreateRequest;
import com.example.todo.dto.external.ExternalTaskResponse;
import com.example.todo.service.tasks.CreatedTask;
import com.example.todo.service.tasks.TasksGatewayService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
public class TasksGatewayController {

    private final TasksGatewayService tasksGatewayService;

    public TasksGatewayController(TasksGatewayService tasksGatewayService) {
        this.tasksGatewayService = tasksGatewayService;
    }

    @PostMapping
    public ResponseEntity<ExternalTaskResponse> create(@Valid @RequestBody ExternalTaskCreateRequest body) {
        CreatedTask created = tasksGatewayService.createTask(body);
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.CREATED);
        if (created.location() != null) {
            builder = builder.location(created.location());
        }
        return builder.body(created.task());
    }

    @GetMapping("/{id}")
    public ExternalTaskResponse getById(@PathVariable long id) {
        return tasksGatewayService.getTask(id);
    }

    @GetMapping
    public List<ExternalTaskResponse> list(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Integer limit) {
        return tasksGatewayService.listTasks(completed, limit);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        tasksGatewayService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
