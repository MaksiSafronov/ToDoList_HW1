package com.example.todo.service;

import com.example.todo.exception.TaskNotFoundException;
import com.example.todo.model.Task;
import com.example.todo.model.TaskAttachment;
import com.example.todo.repository.TaskAttachmentRepository;
import com.example.todo.repository.TaskRepository;
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
	private final TaskRepository taskRepository;
	private final Path uploadDirPath;

	public AttachmentService(TaskAttachmentRepository taskAttachmentRepository,
			TaskRepository taskRepository,
			@Value("${app.upload-dir:uploads}") String uploadDir) {
		this.taskAttachmentRepository = taskAttachmentRepository;
		this.taskRepository = taskRepository;
		this.uploadDirPath = Path.of(uploadDir).toAbsolutePath().normalize();
		try {
			Files.createDirectories(this.uploadDirPath);
		} catch (IOException e) {
			throw new IllegalStateException("Could not initialize upload directory", e);
		}
	}

	/**
	 * Сохраняет файл на диск, затем метаданные в БД. Если запись в БД не удалась, загруженный файл удаляется.
	 */
	public TaskAttachment storeAttachment(Long taskId, MultipartFile file) {
		if (taskId == null) {
			throw new IllegalArgumentException("Task id must not be null");
		}
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File must not be empty");
		}
		if (!taskRepository.existsById(taskId)) {
			throw new TaskNotFoundException(taskId);
		}

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

		Task taskRef = taskRepository.getReferenceById(taskId);
		TaskAttachment attachment = new TaskAttachment();
		attachment.setTask(taskRef);
		attachment.setFileName(originalFileName);
		attachment.setStoredFileName(storedFileName);
		attachment.setContentType(file.getContentType());
		attachment.setSize(file.getSize());
		attachment.setUploadedAt(LocalDateTime.now());

		try {
			return taskAttachmentRepository.save(attachment);
		} catch (RuntimeException e) {
			try {
				Files.deleteIfExists(targetPath);
			} catch (IOException cleanup) {
				e.addSuppressed(cleanup);
			}
			throw e;
		}
	}

	public TaskAttachment getAttachment(Long attachmentId) {
		return taskAttachmentRepository.findById(attachmentId)
				.orElseThrow(() -> new IllegalArgumentException("Attachment not found: " + attachmentId));
	}

	public List<TaskAttachment> getAttachmentsByTaskId(Long taskId) {
		if (taskId == null) {
			throw new IllegalArgumentException("Task id must not be null");
		}
		if (!taskRepository.existsById(taskId)) {
			throw new TaskNotFoundException(taskId);
		}
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

	/**
	 * Сначала удаляется строка в БД, затем файл на диске, чтобы не оставать с метаданными без файла при сбое удаления файла.
	 */
	public void deleteAttachment(Long attachmentId) {
		TaskAttachment attachment = getAttachment(attachmentId);
		Path filePath = uploadDirPath.resolve(attachment.getStoredFileName()).normalize();
		taskAttachmentRepository.deleteById(attachmentId);
		try {
			Files.deleteIfExists(filePath);
		} catch (IOException e) {
			throw new IllegalStateException("Could not delete file for attachment: " + attachmentId, e);
		}
	}
}
