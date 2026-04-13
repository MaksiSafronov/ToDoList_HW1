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
 * Срез JPA с профилем {@code test} (H2 in-memory, Flyway), см. {@code application.yaml}.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class TaskAttachmentRepositoryTest {

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
	void saveTaskThenAttachment_persistsForeignKeyAndFindByTaskId() {
		Task task = new Task();
		task.setTitle("with-file");
		task.setDescription("desc");
		task.setCompleted(false);
		task.setDueDate(LocalDate.now().plusDays(1));
		task.setPriority(Priority.MEDIUM);
		task.setTags(Set.of("x"));
		Task savedTask = taskRepository.saveAndFlush(task);

		TaskAttachment attachment = new TaskAttachment();
		attachment.setTask(savedTask);
		attachment.setFileName("report.pdf");
		attachment.setStoredFileName("uuid-report.pdf");
		attachment.setContentType("application/pdf");
		attachment.setSize(2048);
		attachment.setUploadedAt(LocalDateTime.of(2026, 3, 1, 10, 0));
		TaskAttachment savedAtt = taskAttachmentRepository.saveAndFlush(attachment);

		assertThat(savedAtt.getId()).isNotNull();
		assertThat(savedAtt.getTask().getId()).isEqualTo(savedTask.getId());

		entityManager.flush();
		entityManager.clear();

		List<TaskAttachment> byTask = taskAttachmentRepository.findByTask_Id(savedTask.getId());
		assertThat(byTask).hasSize(1);
		assertThat(byTask.get(0).getFileName()).isEqualTo("report.pdf");
		assertThat(byTask.get(0).getTask().getId()).isEqualTo(savedTask.getId());
	}

	@Test
	void deleteByTaskId_removesAttachmentsForThatTaskOnly() {
		Task t1 = persistBareTask("t1");
		Task t2 = persistBareTask("t2");
		addAttachment(t1, "a1");
		addAttachment(t1, "a2");
		addAttachment(t2, "b1");

		taskAttachmentRepository.deleteByTask_Id(t1.getId());
		taskAttachmentRepository.flush();

		assertThat(taskAttachmentRepository.findByTask_Id(t1.getId())).isEmpty();
		assertThat(taskAttachmentRepository.findByTask_Id(t2.getId())).hasSize(1);
	}

	private Task persistBareTask(String title) {
		Task task = new Task();
		task.setTitle(title);
		task.setDescription("d");
		task.setCompleted(false);
		task.setDueDate(LocalDate.now());
		task.setPriority(Priority.LOW);
		task.setTags(Set.of());
		return taskRepository.saveAndFlush(task);
	}

	private void addAttachment(Task task, String name) {
		TaskAttachment a = new TaskAttachment();
		a.setTask(task);
		a.setFileName(name);
		a.setStoredFileName("s-" + name);
		a.setSize(1);
		a.setUploadedAt(LocalDateTime.now());
		taskAttachmentRepository.saveAndFlush(a);
	}
}
