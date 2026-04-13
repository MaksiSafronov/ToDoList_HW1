package com.example.todo.repository;

import com.example.todo.model.Priority;
import com.example.todo.model.Task;
import com.example.todo.model.TaskAttachment;
import com.example.todo.persistence.JpaAuditingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Срез JPA: H2 и миграции Flyway берутся из профиля {@code test} в {@code application.yaml};
 * Аннотация {@link AutoConfigureTestDatabase} с {@link AutoConfigureTestDatabase.Replace#NONE} отключает подмену источника данных на встроенную БД по умолчанию.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class TaskRepositoryTest {

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private TaskAttachmentRepository taskAttachmentRepository;

	@Autowired
	private TestEntityManager entityManager;

	@BeforeEach
	void clear() {
		taskAttachmentRepository.deleteAll();
		taskRepository.deleteAll();
	}

	@Test
	void findTasksDueWithinNextSevenDays_respectsHalfOpenIntervalAndNullDueDate() {
		LocalDate start = LocalDate.of(2026, 6, 10);
		LocalDate endExclusive = start.plusDays(7);

		Task inside = persistTask("in", false, LocalDate.of(2026, 6, 12));
		persistTask("onEnd", false, endExclusive);
		persistTask("before", false, start.minusDays(1));
		persistTask("noDue", false, null);

		List<Task> found = taskRepository.findTasksDueWithinNextSevenDays(start, endExclusive);

		assertThat(found).extracting(Task::getId).containsExactly(inside.getId());
	}

	@Test
	void findAllWithAttachments_loadsAttachmentCollectionsFromDatabase() {
		Task t0 = persistTask("no-att", false, null);
		Task t1 = persistTask("one", false, null);
		persistAttachment(t1, "a.txt");
		Task t2 = persistTask("two", false, null);
		persistAttachment(t2, "b.txt");
		persistAttachment(t2, "c.txt");

		entityManager.flush();
		entityManager.clear();

		List<Task> loaded = taskRepository.findAllWithAttachments();

		assertThat(loaded).hasSize(3);
		assertThat(loaded.stream().filter(t -> t.getId().equals(t0.getId())).findFirst()).hasValueSatisfying(t ->
				assertThat(t.getAttachments()).isEmpty());
		assertThat(loaded.stream().filter(t -> t.getId().equals(t1.getId())).findFirst()).hasValueSatisfying(t ->
				assertThat(t.getAttachments()).hasSize(1));
		assertThat(loaded.stream().filter(t -> t.getId().equals(t2.getId())).findFirst()).hasValueSatisfying(t ->
				assertThat(t.getAttachments()).extracting(TaskAttachment::getFileName).containsExactlyInAnyOrder("b.txt", "c.txt"));
	}

	@Test
	void findByCompletedAndPriority_filtersCorrectly() {
		persistTask("done-high", true, Priority.HIGH, null);
		Task openHigh = persistTask("open-high", false, Priority.HIGH, null);
		persistTask("open-low", false, Priority.LOW, null);

		List<Task> result = taskRepository.findByCompletedAndPriority(false, Priority.HIGH);

		assertThat(result).extracting(Task::getId).containsExactly(openHigh.getId());
	}

	private Task persistTask(String title, boolean completed, LocalDate dueDate) {
		return persistTask(title, completed, Priority.MEDIUM, dueDate);
	}

	private Task persistTask(String title, boolean completed, Priority priority, LocalDate dueDate) {
		Task task = new Task();
		task.setTitle(title);
		task.setDescription("d");
		task.setCompleted(completed);
		task.setDueDate(dueDate);
		task.setPriority(priority);
		task.setTags(Set.of());
		return taskRepository.saveAndFlush(task);
	}

	private void persistAttachment(Task task, String fileName) {
		TaskAttachment a = new TaskAttachment();
		a.setTask(task);
		a.setFileName(fileName);
		a.setStoredFileName("stored-" + fileName);
		a.setSize(1);
		a.setUploadedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
		taskAttachmentRepository.saveAndFlush(a);
	}
}
