package com.example.todo.repository;

import com.example.todo.model.TaskAttachment;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory реализация {@link TaskAttachmentRepository} для хранения метаданных вложений.
 */
@Repository
public class InMemoryTaskAttachmentRepository implements TaskAttachmentRepository {

    private final Map<Long, TaskAttachment> attachments = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong();

    @Override
    public TaskAttachment create(TaskAttachment attachment) {
        if (attachment.getId() == null) {
            attachment.setId(idSequence.incrementAndGet());
        }
        attachments.put(attachment.getId(), attachment);
        return attachment;
    }

    @Override
    public Optional<TaskAttachment> findById(Long id) {
        return Optional.ofNullable(attachments.get(id));
    }

    @Override
    public List<TaskAttachment> findByTaskId(Long taskId) {
        List<TaskAttachment> result = new ArrayList<>();
        for (TaskAttachment attachment : attachments.values()) {
            if (taskId != null && taskId.equals(attachment.getTaskId())) {
                result.add(attachment);
            }
        }
        return result;
    }

    @Override
    public List<TaskAttachment> findAll() {
        return new ArrayList<>(attachments.values());
    }

    @Override
    public void deleteById(Long id) {
        attachments.remove(id);
    }

    @Override
    public void deleteByTaskId(Long taskId) {
        List<Long> idsToDelete = new ArrayList<>();
        for (Map.Entry<Long, TaskAttachment> entry : attachments.entrySet()) {
            TaskAttachment attachment = entry.getValue();
            if (taskId != null && taskId.equals(attachment.getTaskId())) {
                idsToDelete.add(entry.getKey());
            }
        }
        for (Long id : idsToDelete) {
            attachments.remove(id);
        }
    }
}
