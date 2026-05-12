package com.example.todo.service;

import com.example.todo.dto.TaskCountByPriorityDto;
import com.example.todo.model.Priority;
import com.example.todo.model.Task;
import com.example.todo.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskStatisticsJdbcServiceTest {

	@Autowired
	private TaskStatisticsJdbcService taskStatisticsJdbcService;

	@Autowired
	private TaskRepository taskRepository;

	@BeforeEach
	void setUp() {
		taskRepository.deleteAll();
	}

	@Test
	void getTasksCountByPriority_returnsCountsPerPriority() {
		taskRepository.save(buildTask("a", Priority.LOW));
		taskRepository.save(buildTask("b", Priority.LOW));
		taskRepository.save(buildTask("c", Priority.HIGH));

		List<TaskCountByPriorityDto> stats = taskStatisticsJdbcService.getTasksCountByPriority();

		Map<Priority, Long> byPriority = stats.stream()
				.collect(Collectors.toMap(TaskCountByPriorityDto::priority, TaskCountByPriorityDto::taskCount));

		assertThat(byPriority).containsEntry(Priority.LOW, 2L).containsEntry(Priority.HIGH, 1L);
		assertThat(byPriority).doesNotContainKey(Priority.MEDIUM);
	}

	private static Task buildTask(String title, Priority priority) {
		Task t = new Task();
		t.setTitle(title);
		t.setCompleted(false);
		t.setPriority(priority);
		return t;
	}
}
