package com.example.todo.controller;

import com.example.todo.dto.TaskCreateDto;
import com.example.todo.dto.TaskResponseDto;
import com.example.todo.dto.TaskUpdateDto;
import com.example.todo.dto.validation.OnCreate;
import com.example.todo.dto.validation.OnUpdate;
import com.example.todo.exception.TaskNotFoundException;
import com.example.todo.mapper.TaskMapper;
import com.example.todo.model.Task;
import com.example.todo.service.TaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST-контроллер для управления задачами через CRUD API.
 * Базовый путь: {@code /api/tasks}.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TaskController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAll() {
        List<TaskResponseDto> body = taskService.findAll().stream()
                .map(taskMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getById(@PathVariable Long id) {
        Task task = taskService.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        return ResponseEntity.ok(taskMapper.toResponseDto(task));
    }

    @PostMapping
    public ResponseEntity<TaskResponseDto> create(@Validated(OnCreate.class) @RequestBody TaskCreateDto dto) {
        Task task = taskMapper.toEntity(dto);
        Task created = taskService.create(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskMapper.toResponseDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> update(@PathVariable Long id,
                                                  @Validated(OnUpdate.class) @RequestBody TaskUpdateDto dto) {
        Task taskToUpdate = taskService.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        taskMapper.updateEntity(dto, taskToUpdate);
        taskToUpdate.setId(id);
        Task updated = taskService.update(taskToUpdate);
        return ResponseEntity.ok(taskMapper.toResponseDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        taskService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

