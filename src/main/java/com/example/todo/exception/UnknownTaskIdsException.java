package com.example.todo.exception;

import java.util.List;

/**
 * Выбрасывается при массовом обновлении, если среди переданных идентификаторов есть отсутствующие в БД задачи.
 */
public class UnknownTaskIdsException extends RuntimeException {

	private final List<Long> unknownTaskIds;

	public UnknownTaskIdsException(List<Long> unknownTaskIds) {
		super("Tasks not found for ids: " + unknownTaskIds);
		this.unknownTaskIds = List.copyOf(unknownTaskIds);
	}

	public List<Long> getUnknownTaskIds() {
		return unknownTaskIds;
	}
}
