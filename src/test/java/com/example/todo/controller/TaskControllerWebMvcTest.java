package com.example.todo.controller;

import com.example.todo.dto.TaskResponseDto;
import com.example.todo.exception.GlobalExceptionHandler;
import com.example.todo.mapper.TaskMapper;
import com.example.todo.model.Priority;
import com.example.todo.model.Task;
import com.example.todo.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(GlobalExceptionHandler.class)
class TaskControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TaskMapper taskMapper;

    @Test
    void createTask_positive_returns201AndJson() throws Exception {
        Task mappedRequest = new Task();
        mappedRequest.setTitle("New task");
        mappedRequest.setDescription("Description");
        mappedRequest.setPriority(Priority.MEDIUM);
        mappedRequest.setCompleted(false);

        Task created = new Task();
        created.setId(5L);
        created.setTitle("New task");
        created.setDescription("Description");
        created.setPriority(Priority.MEDIUM);
        created.setCompleted(false);
        created.setDueDate(LocalDate.now().plusDays(1));
        created.setTags(Set.of("work"));

        TaskResponseDto responseDto = new TaskResponseDto();
        responseDto.setId(5L);
        responseDto.setTitle("New task");
        responseDto.setDescription("Description");
        responseDto.setPriority(Priority.MEDIUM);
        responseDto.setCompleted(false);
        responseDto.setDueDate(created.getDueDate());
        responseDto.setTags(created.getTags());

        given(taskMapper.toEntity(any())).willReturn(mappedRequest);
        given(taskService.create(any(Task.class))).willReturn(created);
        given(taskMapper.toResponseDto(created)).willReturn(responseDto);

        String body = """
                {
                  "title": "New task",
                  "description": "Description",
                  "dueDate": "%s",
                  "priority": "MEDIUM",
                  "tags": ["work"]
                }
                """.formatted(created.getDueDate());

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("New task"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void getById_positive_returns200AndJson() throws Exception {
        Task task = new Task();
        task.setId(10L);
        task.setTitle("Existing task");
        task.setDescription("Stored description");
        task.setPriority(Priority.HIGH);
        task.setCompleted(true);

        TaskResponseDto responseDto = new TaskResponseDto();
        responseDto.setId(10L);
        responseDto.setTitle("Existing task");
        responseDto.setDescription("Stored description");
        responseDto.setPriority(Priority.HIGH);
        responseDto.setCompleted(true);

        given(taskService.findById(10L)).willReturn(Optional.of(task));
        given(taskMapper.toResponseDto(task)).willReturn(responseDto);

        mockMvc.perform(get("/api/tasks/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Existing task"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void createTask_invalidPayload_returns400() throws Exception {
        String invalidBody = """
                {
                  "title": "ab",
                  "description": "Description",
                  "dueDate": "%s",
                  "priority": "MEDIUM",
                  "tags": []
                }
                """.formatted(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.title").exists());
    }
}
