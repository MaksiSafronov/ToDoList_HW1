package com.example.todo.controller;

import com.example.todo.model.Task;
import com.example.todo.service.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private TaskService taskService;

    @Test
    void getAllTasks_positive() {
        Task t1 = new Task();
        t1.setId(1L);
        t1.setTitle("t1");
        t1.setDescription("d1");
        t1.setCompleted(false);

        Task t2 = new Task();
        t2.setId(2L);
        t2.setTitle("t2");
        t2.setDescription("d2");
        t2.setCompleted(true);

        given(taskService.findAll()).willReturn(List.of(t1, t2));

        ResponseEntity<Task[]> response = restTemplate.getForEntity("/api/tasks", Task[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()[0].getId()).isEqualTo(1L);
        assertThat(response.getBody()[1].getId()).isEqualTo(2L);
    }

    @Test
    void getAllTasks_negative_serviceFailure() {
        when(taskService.findAll()).thenThrow(new RuntimeException("fail"));

        ResponseEntity<String> response = restTemplate.getForEntity("/api/tasks", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getTaskById_positive() {
        Task t1 = new Task();
        t1.setId(10L);
        t1.setTitle("title");
        t1.setDescription("desc");
        t1.setCompleted(false);

        given(taskService.findById(10L)).willReturn(Optional.of(t1));

        ResponseEntity<Task> response = restTemplate.getForEntity("/api/tasks/10", Task.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(10L);
        assertThat(response.getBody().getTitle()).isEqualTo("title");
    }

    @Test
    void getTaskById_negative_notFound() {
        given(taskService.findById(999L)).willReturn(Optional.empty());

        ResponseEntity<Task> response = restTemplate.getForEntity("/api/tasks/999", Task.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createTask_positive() {
        Task request = new Task();
        request.setTitle("new");
        request.setDescription("desc");
        request.setCompleted(false);

        Task created = new Task();
        created.setId(5L);
        created.setTitle("new");
        created.setDescription("desc");
        created.setCompleted(false);

        given(taskService.create(any(Task.class))).willReturn(created);

        ResponseEntity<Task> response = restTemplate.postForEntity("/api/tasks", request, Task.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(5L);
        assertThat(response.getBody().getTitle()).isEqualTo("new");
    }

    @Test
    void createTask_negative_invalidJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{", headers);

        ResponseEntity<String> response = restTemplate.exchange("/api/tasks", HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateTask_positive() {
        Task existing = new Task();
        existing.setId(7L);
        existing.setTitle("old");
        existing.setDescription("old");
        existing.setCompleted(false);

        Task request = new Task();
        request.setTitle("new");
        request.setDescription("new");
        request.setCompleted(true);

        Task updated = new Task();
        updated.setId(7L);
        updated.setTitle("new");
        updated.setDescription("new");
        updated.setCompleted(true);

        given(taskService.findById(7L)).willReturn(Optional.of(existing));
        given(taskService.update(any(Task.class))).willReturn(updated);

        ResponseEntity<Task> response = restTemplate.exchange("/api/tasks/7", HttpMethod.PUT, new HttpEntity<>(request), Task.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(7L);
        assertThat(response.getBody().isCompleted()).isTrue();

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).update(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(7L);
    }

    @Test
    void updateTask_negative_notFound() {
        Task request = new Task();
        request.setTitle("x");
        request.setDescription("y");
        request.setCompleted(false);

        given(taskService.findById(404L)).willReturn(Optional.empty());

        ResponseEntity<Task> response = restTemplate.exchange("/api/tasks/404", HttpMethod.PUT, new HttpEntity<>(request), Task.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteTask_positive() {
        Task existing = new Task();
        existing.setId(11L);
        existing.setTitle("t");
        existing.setDescription("d");
        existing.setCompleted(false);

        given(taskService.findById(11L)).willReturn(Optional.of(existing));
        doNothing().when(taskService).deleteById(11L);

        ResponseEntity<Void> response = restTemplate.exchange("/api/tasks/11", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(taskService).deleteById(eq(11L));
    }

    @Test
    void deleteTask_negative_notFound() {
        given(taskService.findById(12L)).willReturn(Optional.empty());

        ResponseEntity<Void> response = restTemplate.exchange("/api/tasks/12", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

