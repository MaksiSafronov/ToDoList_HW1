package com.example.todo.repository;

import com.example.todo.model.Task;
import java.util.List;
import java.util.Optional;

/**
 * Контракт репозитория задач с базовыми CRUD-операциями.
 */
public interface TaskRepository {

    Task create(Task task);

    Optional<Task> findById(Long id);

    List<Task> findAll();

    Task update(Task task);

    void deleteById(Long id);
}

