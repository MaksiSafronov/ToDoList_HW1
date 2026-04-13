package com.example.todo.dto;

import com.example.todo.dto.validation.DueDateNotBeforeCreation;
import com.example.todo.dto.validation.OnUpdate;
import com.example.todo.model.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

@DueDateNotBeforeCreation(groups = OnUpdate.class)
@Schema(name = "TaskUpdateDto", description = "Частичное обновление задачи; поля опциональны (id передаётся в пути)")
public class TaskUpdateDto {

    @Size(min = 3, max = 100, groups = OnUpdate.class)
    @Schema(description = "Заголовок", minLength = 3, maxLength = 100)
    private String title;

    @Size(max = 500, groups = OnUpdate.class)
    @Schema(description = "Описание", maxLength = 500)
    private String description;

    @Schema(description = "Признак выполнения")
    private Boolean completed;

    @FutureOrPresent(groups = OnUpdate.class)
    @Schema(description = "Срок выполнения (не раньше даты создания задачи)", example = "2026-04-01")
    private LocalDate dueDate;

    @Schema(description = "Приоритет")
    private Priority priority;

    @Size(max = 5, groups = OnUpdate.class)
    @Schema(description = "Теги (не более 5)")
    private Set<String> tags;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
}
