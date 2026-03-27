package com.example.todo.repository;

import com.example.todo.model.TaskAttachment;

import java.util.List;
import java.util.Optional;

/**
 * Контракт репозитория метаданных вложений задач.
 */
public interface TaskAttachmentRepository {

    TaskAttachment create(TaskAttachment attachment);

    Optional<TaskAttachment> findById(Long id);

    List<TaskAttachment> findByTaskId(Long taskId);

    List<TaskAttachment> findAll();

    void deleteById(Long id);

    void deleteByTaskId(Long taskId);
}
