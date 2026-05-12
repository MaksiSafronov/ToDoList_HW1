package com.example.todo.service;

import com.example.todo.model.Priority;
import com.example.todo.model.Task;
import com.example.todo.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(TaskServiceTest.CorsTestConfig.class)
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @MockitoBean
    private TaskRepository taskRepository;

    @Test
    void bulkCompleteTasks_existingTask_updatesStatusAndPersists() {
        // given
        Task existing = new Task();
        existing.setId(1L);
        existing.setTitle("Test task");
        existing.setPriority(Priority.MEDIUM);
        existing.setCompleted(false);

        when(taskRepository.findAllById(any())).thenReturn(List.of(existing));

        // when
        taskService.bulkCompleteTasks(List.of(1L));

        // then
        verify(taskRepository).findAllById(any());

        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<List<Task>> saveAllCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(taskRepository).saveAll(saveAllCaptor.capture());

        List<Task> savedTasks = saveAllCaptor.getValue();
        assertThat(savedTasks).hasSize(1);
        assertThat(savedTasks.getFirst().getId()).isEqualTo(1L);
        assertThat(savedTasks.getFirst().isCompleted()).isTrue();

        List<Long> idsPassedToRepository = new ArrayList<>();
        Iterable<Long> capturedIds = captureFindAllByIdArgument();
        capturedIds.forEach(idsPassedToRepository::add);
        assertThat(idsPassedToRepository).containsExactly(1L);
    }

    private Iterable<Long> captureFindAllByIdArgument() {
        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<Iterable<Long>> idsCaptor = org.mockito.ArgumentCaptor.forClass(Iterable.class);
        verify(taskRepository).findAllById(idsCaptor.capture());
        return idsCaptor.getValue();
    }

    @TestConfiguration
    static class CorsTestConfig {
        @Bean
        CorsConfigurationSource corsConfigurationSource() {
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", new CorsConfiguration());
            return source;
        }
    }
}
