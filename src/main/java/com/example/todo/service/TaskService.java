package com.example.todo.service;

import com.example.todo.model.Task;
import com.example.todo.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Сервисный слой для CRUD-операций над задачами.
 * Использует {@link com.example.todo.repository.TaskRepository} как источник данных и поддерживает простой кэш в памяти.
 */
@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final Map<String, Task> taskCache = new HashMap<>();

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @PostConstruct
    public void initCache() {
        List<Task> tasks = taskRepository.findAll();
        for (Task task : tasks) {
            if (task.getId() != null) {
                taskCache.put(task.getId().toString(), task);
            }
        }
        logger.info("Task cache initialized with {} tasks for {} {}", taskCache.size(), appName, appVersion);
    }

    @PreDestroy
    public void cleanup() {
        logger.info("Cleaning up TaskService, cache size is {}", taskCache.size());
        taskCache.clear();
    }

    public Task create(Task task) {
        Task created = taskRepository.create(task);
        if (created.getId() != null) {
            taskCache.put(created.getId().toString(), created);
        }
        return created;
    }

    public Optional<Task> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        Task cached = taskCache.get(id.toString());
        if (cached != null) {
            return Optional.of(cached);
        }
        Optional<Task> found = taskRepository.findById(id);
        found.ifPresent(task -> taskCache.put(id.toString(), task));
        return found;
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Task update(Task task) {
        Task updated = taskRepository.update(task);
        if (updated.getId() != null) {
            taskCache.put(updated.getId().toString(), updated);
        }
        return updated;
    }

    public void deleteById(Long id) {
        taskRepository.deleteById(id);
        if (id != null) {
            taskCache.remove(id.toString());
        }
    }
}

