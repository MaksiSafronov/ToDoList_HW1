package com.example.todo.service.tasks;

import com.example.todo.client.ExternalTasksClient;
import com.example.todo.dto.external.ExternalTaskCreateRequest;
import com.example.todo.dto.external.ExternalTaskResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TasksGatewayServiceImpl implements TasksGatewayService {

    private final ExternalTasksClient externalTasksClient;

    public TasksGatewayServiceImpl(ExternalTasksClient externalTasksClient) {
        this.externalTasksClient = externalTasksClient;
    }

    @Override
    public CreatedTask createTask(ExternalTaskCreateRequest request) {
        return externalTasksClient.createTask(request);
    }

    @Override
    public ExternalTaskResponse getTask(long id) {
        return externalTasksClient.getTask(id);
    }

    @Override
    public List<ExternalTaskResponse> listTasks(Boolean completed, Integer limit) {
        return externalTasksClient.listTasks(completed, limit);
    }

    @Override
    public void deleteTask(long id) {
        externalTasksClient.deleteTask(id);
    }
}
