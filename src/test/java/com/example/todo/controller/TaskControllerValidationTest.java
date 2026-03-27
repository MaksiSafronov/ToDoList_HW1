package com.example.todo.controller;

import com.example.todo.exception.GlobalExceptionHandler;
import com.example.todo.mapper.TaskMapper;
import com.example.todo.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class)
@Import(GlobalExceptionHandler.class)
class TaskControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;
    @MockBean
    private TaskMapper taskMapper;

    @Test
    void createTask_invalidTitleLength_returns400WithErrorBody() throws Exception {
        String body = """
                {
                  "title": "ab",
                  "description": "ok",
                  "dueDate": "2026-12-31",
                  "priority": "MEDIUM",
                  "tags": []
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.details.title").exists());
    }

    @Test
    void createTask_missingPriority_returns400() throws Exception {
        String body = """
                {
                  "title": "Valid title",
                  "description": "d",
                  "dueDate": "2026-12-31"
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createTask_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateTask_invalidTitle_returns400() throws Exception {
        String body = """
                {
                  "title": "x"
                }
                """;

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
