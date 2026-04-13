package com.example.todo.controller;

import com.example.todo.dto.ErrorResponse;
import com.example.todo.dto.TaskResponseDto;
import com.example.todo.exception.TaskNotFoundException;
import com.example.todo.mapper.TaskMapper;
import com.example.todo.service.FavoritesService;
import com.example.todo.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * Демонстрирует внедрение {@link HttpSession}: Spring создаёт/привязывает сессию к запросу,
 * а {@link FavoritesService} хранит список избранных id в атрибутах этой сессии.
 */
@Tag(name = "Favorites", description = "Избранные задачи (сессия; для кук используйте credentials)")
@RestController
@RequestMapping("/api/favorites")
public class FavoritesController {

    private final FavoritesService favoritesService;
    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public FavoritesController(FavoritesService favoritesService,
                               TaskService taskService,
                               TaskMapper taskMapper) {
        this.favoritesService = favoritesService;
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @Operation(summary = "Добавить задачу в избранное")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Добавлено в избранное"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{taskId}")
    public ResponseEntity<Void> addToFavorites(
            @Parameter(description = "Идентификатор задачи", required = true) @PathVariable Long taskId,
            HttpSession session) {
        taskService.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        favoritesService.addToFavorites(taskId, session);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Убрать задачу из избранного")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Удалено из избранного")
    })
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> removeFromFavorites(
            @Parameter(description = "Идентификатор задачи", required = true) @PathVariable Long taskId,
            HttpSession session) {
        favoritesService.removeFromFavorites(taskId, session);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Список избранных задач", description = "Требует сессию с ранее добавленными id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список задач",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskResponseDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getFavorites(HttpSession session) {
        List<TaskResponseDto> body = favoritesService.getFavoriteTaskIds(session).stream()
                .map(taskService::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(taskMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(body);
    }
}
