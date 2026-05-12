package com.example.todo.controller;

import com.example.todo.dto.ErrorResponse;
import com.example.todo.dto.TaskCreateDto;
import com.example.todo.dto.TaskResponseDto;
import com.example.todo.dto.TaskUpdateDto;
import com.example.todo.dto.TaskWithAttachmentsResponseDto;
import com.example.todo.dto.validation.OnCreate;
import com.example.todo.dto.validation.OnUpdate;
import com.example.todo.exception.TaskNotFoundException;
import com.example.todo.mapper.TaskMapper;
import com.example.todo.model.Task;
import com.example.todo.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tasks", description = "CRUD операции над задачами")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    public static final String X_TOTAL_COUNT = "X-Total-Count";

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TaskController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @Operation(summary = "Список всех задач", description = "В ответе заголовок X-Total-Count содержит общее число задач")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список задач",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskResponseDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAll() {
        List<Task> tasks = taskService.findAll();
        List<TaskResponseDto> body = tasks.stream()
                .map(taskMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok()
                .header(X_TOTAL_COUNT, String.valueOf(tasks.size()))
                .body(body);
    }

    @Operation(summary = "Список задач с вложениями",
            description = "Задачи и вложения загружаются без N+1 (JOIN FETCH на уровне репозитория)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список задач с вложениями",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskWithAttachmentsResponseDto.class))))
    })
    @GetMapping("/with-attachments")
    public ResponseEntity<List<TaskWithAttachmentsResponseDto>> getAllWithAttachments() {
        List<Task> tasks = taskService.findAllWithAttachments();
        List<TaskWithAttachmentsResponseDto> body = tasks.stream()
                .map(taskMapper::toResponseWithAttachments)
                .toList();
        return ResponseEntity.ok()
                .header(X_TOTAL_COUNT, String.valueOf(tasks.size()))
                .body(body);
    }

    @Operation(summary = "Получить задачу по id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Задача найдена",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Задача не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getById(
            @Parameter(description = "Идентификатор задачи", required = true) @PathVariable Long id) {
        Task task = taskService.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        return ResponseEntity.ok(taskMapper.toResponseDto(task));
    }

    @Operation(summary = "Создать задачу")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Задача создана",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<TaskResponseDto> create(@Validated(OnCreate.class) @RequestBody TaskCreateDto dto) {
        Task task = taskMapper.toEntity(dto);
        Task created = taskService.create(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskMapper.toResponseDto(created));
    }

    @Operation(summary = "Обновить задачу")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Задача обновлена",
                    content = @Content(schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Задача не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> update(
            @Parameter(description = "Идентификатор задачи", required = true) @PathVariable Long id,
            @Validated(OnUpdate.class) @RequestBody TaskUpdateDto dto) {
        Task taskToUpdate = taskService.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        taskMapper.updateEntity(dto, taskToUpdate);
        taskToUpdate.setId(id);
        Task updated = taskService.update(taskToUpdate);
        return ResponseEntity.ok(taskMapper.toResponseDto(updated));
    }

    @Operation(summary = "Удалить задачу")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Удалено"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Идентификатор задачи", required = true) @PathVariable Long id) {
        taskService.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        taskService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
