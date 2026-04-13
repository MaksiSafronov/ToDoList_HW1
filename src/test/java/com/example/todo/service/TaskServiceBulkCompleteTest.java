package com.example.todo.service;

import com.example.todo.exception.UnknownTaskIdsException;
import com.example.todo.model.Priority;
import com.example.todo.model.Task;
import com.example.todo.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class TaskServiceBulkCompleteTest {

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskRepository taskRepository;

	@Test
	void bulkCompleteTasks_marksAllCompleted_whenAllIdsExist() {
		Task a = persistTask("A", false);
		Task b = persistTask("B", false);

		taskService.bulkCompleteTasks(List.of(a.getId(), b.getId()));

		assertThat(taskRepository.findById(a.getId())).hasValueSatisfying(t -> assertThat(t.isCompleted()).isTrue());
		assertThat(taskRepository.findById(b.getId())).hasValueSatisfying(t -> assertThat(t.isCompleted()).isTrue());
	}

	@Test
	void bulkCompleteTasks_rollsBack_whenAnyIdMissing() {
		Task a = persistTask("Only", false);
		Long missingId = a.getId() + 10_000L;

		assertThatThrownBy(() -> taskService.bulkCompleteTasks(List.of(a.getId(), missingId)))
				.isInstanceOf(UnknownTaskIdsException.class)
				.extracting(ex -> ((UnknownTaskIdsException) ex).getUnknownTaskIds())
				.isEqualTo(List.of(missingId));

		assertThat(taskRepository.findById(a.getId())).hasValueSatisfying(t -> assertThat(t.isCompleted()).isFalse());
	}

	@Test
	void bulkCompleteTasks_emptyList_noOp() {
		taskService.bulkCompleteTasks(List.of());
	}

	private Task persistTask(String title, boolean completed) {
		Task task = new Task();
		task.setTitle(title);
		task.setDescription("d");
		task.setCompleted(completed);
		task.setDueDate(LocalDate.now().plusDays(1));
		task.setPriority(Priority.MEDIUM);
		task.setTags(Set.of());
		return taskRepository.saveAndFlush(task);
	}
}
