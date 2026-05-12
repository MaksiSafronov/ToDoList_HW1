package com.example.todo.service.tasks;

import com.example.todo.dto.external.ExternalTaskResponse;

import java.net.URI;

public record CreatedTask(ExternalTaskResponse task, URI location) {
}
