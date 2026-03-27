package com.example.todo.controller;

import com.example.todo.dto.TaskResponseDto;
import com.example.todo.exception.TaskNotFoundException;
import com.example.todo.mapper.TaskMapper;
import com.example.todo.service.FavoritesService;
import com.example.todo.service.TaskService;
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

    @PostMapping("/{taskId}")
    public ResponseEntity<Void> addToFavorites(@PathVariable Long taskId,
                                               HttpSession session) {
        taskService.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        favoritesService.addToFavorites(taskId, session);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> removeFromFavorites(@PathVariable Long taskId,
                                                    HttpSession session) {
        favoritesService.removeFromFavorites(taskId, session);
        return ResponseEntity.noContent().build();
    }

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
