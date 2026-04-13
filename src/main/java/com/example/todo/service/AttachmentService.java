package com.example.todo.service;

import com.example.todo.exception.TaskNotFoundException;
import com.example.todo.model.Task;
import com.example.todo.model.TaskAttachment;
import com.example.todo.repository.TaskAttachmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AttachmentService {

    private final TaskAttachmentRepository taskAttachmentRepository;
    private final TaskService taskService;
    private final Path uploadDirPath;

    public AttachmentService(TaskAttachmentRepository taskAttachmentRepository,
                             TaskService taskService,
                             @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.taskAttachmentRepository = taskAttachmentRepository;
        this.taskService = taskService;
        this.uploadDirPath = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDirPath);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize upload directory", e);
        }
    }

    public TaskAttachment storeAttachment(Long taskId, MultipartFile file) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task id must not be null");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        Task task = taskService.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        String originalFileName = StringUtils.cleanPath(
                Optional.ofNullable(file.getOriginalFilename()).orElse("file")
        );
        String extension = "";
        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            extension = originalFileName.substring(lastDotIndex);
        }
        String storedFileName = UUID.randomUUID() + extension;
        Path targetPath = uploadDirPath.resolve(storedFileName).normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Could not store file: " + originalFileName, e);
        }

        TaskAttachment attachment = new TaskAttachment();
        attachment.setTask(task);
        attachment.setFileName(originalFileName);
        attachment.setStoredFileName(storedFileName);
        attachment.setContentType(file.getContentType());
        attachment.setSize(file.getSize());
        attachment.setUploadedAt(LocalDateTime.now());
        return taskAttachmentRepository.save(attachment);
    }

    public TaskAttachment getAttachment(Long attachmentId) {
        return taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found: " + attachmentId));
    }

    public List<TaskAttachment> getAttachmentsByTaskId(Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task id must not be null");
        }
        taskService.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        return taskAttachmentRepository.findByTask_Id(taskId);
    }

    public Resource loadAsResource(Long attachmentId) {
        TaskAttachment attachment = getAttachment(attachmentId);
        Path filePath = uploadDirPath.resolve(attachment.getStoredFileName()).normalize();
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("File not found for attachment: " + attachmentId);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Failed to load attachment file: " + attachmentId, e);
        }
    }

    public void deleteAttachment(Long attachmentId) {
        TaskAttachment attachment = getAttachment(attachmentId);
        Path filePath = uploadDirPath.resolve(attachment.getStoredFileName()).normalize();
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new IllegalStateException("Could not delete file for attachment: " + attachmentId, e);
        }
        taskAttachmentRepository.deleteById(attachmentId);
    }
}
