package com.example.todo.service.tasks;

import com.example.todo.dto.external.ExternalTaskCreateRequest;
import com.example.todo.dto.external.ExternalTaskResponse;

import java.util.List;

public interface TasksGatewayService {

    CreatedTask createTask(ExternalTaskCreateRequest request);

    ExternalTaskResponse getTask(long id);

    List<ExternalTaskResponse> listTasks(Boolean completed, Integer limit);

    void deleteTask(long id);
}
