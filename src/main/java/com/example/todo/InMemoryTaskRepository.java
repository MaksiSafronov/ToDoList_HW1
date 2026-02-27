package com.example.todo;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Primary
public class InMemoryTaskRepository implements TaskRepository {

    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong();

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

