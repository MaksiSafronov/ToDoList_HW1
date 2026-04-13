package com.example.todo.repository;

import com.example.todo.model.Priority;
import com.example.todo.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByCompletedAndPriority(boolean completed, Priority priority);

	List<Task> findByCompleted(boolean completed);

	List<Task> findByPriority(Priority priority);
}
