package com.example.todo.repository;

import com.example.todo.model.Priority;
import com.example.todo.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

	/**
	 * Все задачи с коллекцией {@code attachments} одним запросом ({@code JOIN FETCH}),
	 * без N+1 при обращении к вложениям.
	 */
	@Query("""
			SELECT DISTINCT t FROM Task t
			LEFT JOIN FETCH t.attachments
			ORDER BY t.id
			""")
	List<Task> findAllWithAttachments();

	List<Task> findByCompletedAndPriority(boolean completed, Priority priority);

	List<Task> findByCompleted(boolean completed);

	List<Task> findByPriority(Priority priority);

	/**
	 * Задачи с заполненным сроком, у которых {@code dueDate} попадает в полуинтервал
	 * {@code [startDate, endDateExclusive)} (типично {@code startDate = today},
	 * {@code endDateExclusive = startDate.plusDays(7)} — ближайшие 7 суток).
	 */
	@Query("""
			SELECT t FROM Task t
			WHERE t.dueDate IS NOT NULL
			  AND t.dueDate >= :startDate
			  AND t.dueDate < :endDateExclusive
			""")
	List<Task> findTasksDueWithinNextSevenDays(
			@Param("startDate") LocalDate startDate,
			@Param("endDateExclusive") LocalDate endDateExclusive);
}
