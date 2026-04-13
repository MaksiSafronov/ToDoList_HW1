package com.example.todo.service;

import com.example.todo.exception.UnknownTaskIdsException;
import com.example.todo.model.Task;
import com.example.todo.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервисный слой для CRUD-операций над задачами через {@link TaskRepository} (Spring Data JPA).
 * Данные читаются и пишутся только из БД, без дублирующего кэша, чтобы не расходиться с JPA.
 */
@Service
@Transactional(readOnly = true)
public class TaskService {

	private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

	private final TaskRepository taskRepository;

	@Value("${app.name}")
	private String appName;

	@Value("${app.version}")
	private String appVersion;

	public TaskService(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@PostConstruct
	public void logStartup() {
		logger.info("TaskService ready: {} task(s) in database for {} {}",
				taskRepository.count(), appName, appVersion);
	}

	@PreDestroy
	public void cleanup() {
		logger.info("Shutting down TaskService");
	}

	@Transactional
	public Task create(Task task) {
		return taskRepository.save(task);
	}

	public Optional<Task> findById(Long id) {
		if (id == null) {
			return Optional.empty();
		}
		return taskRepository.findById(id);
	}

	public List<Task> findAll() {
		return taskRepository.findAll();
	}

	/**
	 * Список задач с загруженными вложениями; выборка из БД без N+1 для коллекции вложений.
	 */
	public List<Task> findAllWithAttachments() {
		return taskRepository.findAllWithAttachments();
	}

	@Transactional
	public Task update(Task task) {
		return taskRepository.save(task);
	}

	@Transactional
	public void deleteById(Long id) {
		if (id != null) {
			taskRepository.deleteById(id);
		}
	}

	/**
	 * Помечает все указанные задачи как выполненные. Операция атомарна: при любом несуществующем id
	 * выбрасывается {@link UnknownTaskIdsException}, транзакция откатывается и ни одна задача не меняется.
	 */
	@Transactional(
			readOnly = false,
			propagation = Propagation.REQUIRED,
			rollbackFor = Exception.class
	)
	public void bulkCompleteTasks(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return;
		}
		List<Long> nonNullIds = ids.stream().filter(Objects::nonNull).toList();
		if (nonNullIds.isEmpty()) {
			return;
		}
		Set<Long> uniqueIds = new LinkedHashSet<>(nonNullIds);
		List<Task> tasks = taskRepository.findAllById(uniqueIds);
		if (tasks.size() != uniqueIds.size()) {
			Set<Long> foundIds = tasks.stream().map(Task::getId).collect(Collectors.toSet());
			List<Long> unknown = uniqueIds.stream()
					.filter(id -> !foundIds.contains(id))
					.sorted()
					.toList();
			throw new UnknownTaskIdsException(unknown);
		}
		for (Task task : tasks) {
			task.setCompleted(true);
		}
		taskRepository.saveAll(tasks);
	}
}
