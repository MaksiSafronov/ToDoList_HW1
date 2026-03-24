package com.example.todo.dto.validation;

import com.example.todo.dto.TaskUpdateDto;
import com.example.todo.model.Task;
import com.example.todo.service.TaskService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
public class DueDateNotBeforeCreationValidator
        implements ConstraintValidator<DueDateNotBeforeCreation, TaskUpdateDto> {

    private final TaskService taskService;

    public DueDateNotBeforeCreationValidator(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public boolean isValid(TaskUpdateDto dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getDueDate() == null) {
            return true;
        }

        Long taskId = extractTaskIdFromPath();
        if (taskId == null) {
            return true;
        }

        Optional<Task> existing = taskService.findById(taskId);
        if (existing.isEmpty()) {
            return true;
        }

        LocalDateTime createdAt = existing.get().getCreatedAt();
        if (createdAt == null) {
            return true;
        }

        LocalDate creationDate = createdAt.toLocalDate();
        boolean valid = !dto.getDueDate().isBefore(creationDate);
        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("dueDate")
                    .addConstraintViolation();
        }
        return valid;
    }

    private Long extractTaskIdFromPath() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        Object uriTemplateVariables = attributes.getRequest()
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(uriTemplateVariables instanceof Map<?, ?> variables)) {
            return null;
        }

        Object idValue = variables.get("id");
        if (idValue == null) {
            return null;
        }

        try {
            return Long.parseLong(idValue.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
