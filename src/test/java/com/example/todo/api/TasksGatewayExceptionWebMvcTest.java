package com.example.todo.api;

import com.example.todo.exception.ExternalApiException;
import com.example.todo.exception.GlobalExceptionHandler;
import com.example.todo.service.tasks.TasksGatewayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TasksGatewayController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
class TasksGatewayExceptionWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TasksGatewayService tasksGatewayService;

    @Test
    void getTask_whenExternalApiException_returnsMappedStatusAndBody() throws Exception {
        when(tasksGatewayService.getTask(42L))
                .thenThrow(new ExternalApiException(HttpStatus.BAD_GATEWAY, "Upstream gateway failure"));

        mockMvc.perform(get("/api/v1/tasks/42"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Upstream gateway failure"));
    }
}
