package com.example.todo.dto;

import com.example.todo.dto.validation.OnUpdate;
import com.example.todo.model.Priority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public class TaskUpdateDto {

    @Size(min = 3, max = 100, groups = OnUpdate.class)
    private String title;

    @Size(max = 500, groups = OnUpdate.class)
    private String description;
    private Boolean completed;

    @FutureOrPresent(groups = OnUpdate.class)
    private LocalDate dueDate;
    private Priority priority;

    @Size(max = 5, groups = OnUpdate.class)
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
