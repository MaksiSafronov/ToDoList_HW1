package com.example.todo.dto;

import com.example.todo.model.Priority;

/**
 * Строка агрегата: число задач с заданным приоритетом (результат {@code GROUP BY priority}).
 */
public record TaskCountByPriorityDto(Priority priority, long taskCount) {
}
