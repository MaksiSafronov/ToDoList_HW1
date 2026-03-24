package com.example.todo.service;

import com.example.todo.model.Task;
import com.example.todo.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис для демонстрации одновременной инжекции двух реализаций {@link com.example.todo.repository.TaskRepository}
 * с использованием {@code @Primary} и {@code @Qualifier}.
 */
@Service
public class TaskStatisticsService {

    private final TaskRepository primaryRepository;
    private final TaskRepository stubRepository;

    public TaskStatisticsService(TaskRepository taskRepository,
                                 @Qualifier("stubTaskRepository") TaskRepository stubRepository) {
        this.primaryRepository = taskRepository;
        this.stubRepository = stubRepository;
    }

    public int getPrimaryRepositoryTaskCount() {
        return primaryRepository.findAll().size();
    }

    public int getStubRepositoryTaskCount() {
        return stubRepository.findAll().size();
    }

    public List<Task> getPrimaryTasks() {
        return primaryRepository.findAll();
    }

    public List<Task> getStubTasks() {
        return stubRepository.findAll();
    }
}

