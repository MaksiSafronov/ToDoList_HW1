package com.example.todo;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

    Task create(Task task);

    Optional<Task> findById(Long id);

    List<Task> findAll();

    Task update(Task task);

    void deleteById(Long id);
}

