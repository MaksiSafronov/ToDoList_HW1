package com.example.todo.repository;

import com.example.todo.model.Priority;
import com.example.todo.model.Task;
import com.example.todo.persistence.JpaAuditingConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class TaskRepositoryIntegrationTest {

    @SuppressWarnings("resource")
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("todo_test")
            .withUsername("todo")
            .withPassword("todo");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void clear() {
        taskRepository.deleteAll();
    }

    @Test
    void findTasksDueWithinNextSevenDays_usesPostgresAndReturnsOnlyInsideInterval() {
        LocalDate start = LocalDate.of(2026, 6, 10);
        LocalDate endExclusive = start.plusDays(7);

        Task inside = persistTask("inside", false, LocalDate.of(2026, 6, 12));
        persistTask("endBoundaryExcluded", false, endExclusive);
        persistTask("beforeInterval", false, start.minusDays(1));
        persistTask("withoutDueDate", false, null);

        List<Task> found = taskRepository.findTasksDueWithinNextSevenDays(start, endExclusive);

        assertThat(found).extracting(Task::getId).containsExactly(inside.getId());
    }

    private Task persistTask(String title, boolean completed, LocalDate dueDate) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("description");
        task.setCompleted(completed);
        task.setDueDate(dueDate);
        task.setPriority(Priority.MEDIUM);
        task.setTags(Set.of());
        return taskRepository.saveAndFlush(task);
    }
}
