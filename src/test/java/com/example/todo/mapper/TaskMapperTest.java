package com.example.todo.mapper;

import com.example.todo.dto.TaskCreateDto;
import com.example.todo.dto.TaskResponseDto;
import com.example.todo.dto.TaskUpdateDto;
import com.example.todo.model.Priority;
import com.example.todo.model.Task;
import com.example.todo.model.TaskAttachment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskMapperTest {

    @Autowired
    private TaskMapper taskMapper;

    @Test
    void toEntity_mapsCreateDto_andSetsDefaults() {
        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle("My task");
        dto.setDescription("Desc");
        dto.setDueDate(LocalDate.of(2026, 6, 1));
        dto.setPriority(Priority.HIGH);
        dto.setTags(Set.of("a", "b"));

        Task task = taskMapper.toEntity(dto);

        assertThat(task.getId()).isNull();
        assertThat(task.getCreatedAt()).isNull();
        assertThat(task.isCompleted()).isFalse();
        assertThat(task.getTitle()).isEqualTo("My task");
        assertThat(task.getDescription()).isEqualTo("Desc");
        assertThat(task.getDueDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(task.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(task.getTags()).containsExactlyInAnyOrder("a", "b");
        assertThat(task.getAttachments()).isNotNull();
        assertThat(task.getAttachments()).isEmpty();
    }

    @Test
    void updateEntity_partialUpdate_ignoresNullFields() {
        Task existing = new Task();
        existing.setId(10L);
        existing.setTitle("Old");
        existing.setDescription("Old desc");
        existing.setCompleted(false);
        existing.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        existing.setDueDate(LocalDate.of(2026, 2, 1));
        existing.setPriority(Priority.LOW);
        existing.setTags(Set.of("x"));
        TaskAttachment linked = new TaskAttachment();
        linked.setId(100L);
        existing.setAttachments(new ArrayList<>());
        existing.getAttachments().add(linked);

        TaskUpdateDto dto = new TaskUpdateDto();
        dto.setTitle("New title");

        taskMapper.updateEntity(dto, existing);

        assertThat(existing.getId()).isEqualTo(10L);
        assertThat(existing.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 12, 0));
        assertThat(existing.getTitle()).isEqualTo("New title");
        assertThat(existing.getDescription()).isEqualTo("Old desc");
        assertThat(existing.isCompleted()).isFalse();
        assertThat(existing.getAttachments()).hasSize(1);
        assertThat(existing.getAttachments().get(0).getId()).isEqualTo(100L);
    }

    @Test
    void toResponseDto_mapsAllFields() {
        Task task = new Task();
        task.setId(7L);
        task.setTitle("T");
        task.setDescription("D");
        task.setCompleted(true);
        task.setCreatedAt(LocalDateTime.of(2026, 3, 15, 10, 30));
        task.setLastModifiedAt(LocalDateTime.of(2026, 3, 16, 11, 0));
        task.setDueDate(LocalDate.of(2026, 4, 1));
        task.setPriority(Priority.MEDIUM);
        task.setTags(Set.of("t1"));

        TaskResponseDto dto = taskMapper.toResponseDto(task);

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getTitle()).isEqualTo("T");
        assertThat(dto.getDescription()).isEqualTo("D");
        assertThat(dto.isCompleted()).isTrue();
        assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 3, 15, 10, 30));
        assertThat(dto.getLastModifiedAt()).isEqualTo(LocalDateTime.of(2026, 3, 16, 11, 0));
        assertThat(dto.getDueDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(dto.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(dto.getTags()).containsExactly("t1");
    }
}
