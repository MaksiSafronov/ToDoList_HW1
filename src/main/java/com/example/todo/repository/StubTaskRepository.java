package com.example.todo.repository;

import com.example.todo.model.Task;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class StubTaskRepository implements TaskRepository {

    private final Map<Long, Task> tasks = new LinkedHashMap<>();
    private final AtomicLong idSequence = new AtomicLong();

    public StubTaskRepository() {
        Task first = new Task();
        first.setId(1L);
        first.setTitle("Sample task 1");
        first.setDescription("First stub task");
        first.setCompleted(false);

        Task second = new Task();
        second.setId(2L);
        second.setTitle("Sample task 2");
        second.setDescription("Second stub task");
        second.setCompleted(true);

        tasks.put(first.getId(), first);
        tasks.put(second.getId(), second);

        idSequence.set(2L);
    }

    @Override
    public Task create(Task task) {
        if (task.getId() == null) {
            task.setId(idSequence.incrementAndGet());
        }
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(Long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Task update(Task task) {
        if (task.getId() == null) {
            throw new IllegalArgumentException("Task id must not be null for update");
        }
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public void deleteById(Long id) {
        tasks.remove(id);
    }
}

