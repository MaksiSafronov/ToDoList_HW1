package com.example.todo.service;

import com.example.todo.dto.TaskCountByPriorityDto;
import com.example.todo.model.Priority;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class TaskStatisticsJdbcService {

	private static final String COUNT_BY_PRIORITY_SQL = """
			SELECT priority, COUNT(*) AS task_count
			FROM tasks
			GROUP BY priority
			ORDER BY priority
			""";

	/**
	 * Маппинг строки {@code ResultSet} в DTO (демонстрация {@link RowMapper}).
	 */
	private static final class TaskCountByPriorityRowMapper implements RowMapper<TaskCountByPriorityDto> {

		@Override
		public TaskCountByPriorityDto mapRow(ResultSet rs, int rowNum) throws SQLException {
			Priority priority = Priority.valueOf(rs.getString("priority"));
			long taskCount = rs.getLong("task_count");
			return new TaskCountByPriorityDto(priority, taskCount);
		}
	}

	private static final TaskCountByPriorityRowMapper TASK_COUNT_BY_PRIORITY_ROW_MAPPER =
			new TaskCountByPriorityRowMapper();

	private final JdbcTemplate jdbcTemplate;

	public TaskStatisticsJdbcService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional(readOnly = true)
	public List<TaskCountByPriorityDto> getTasksCountByPriority() {
		return jdbcTemplate.query(COUNT_BY_PRIORITY_SQL, TASK_COUNT_BY_PRIORITY_ROW_MAPPER);
	}
}
