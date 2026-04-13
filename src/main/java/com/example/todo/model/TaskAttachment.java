package com.example.todo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "task_attachments")
public class TaskAttachment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "task_id", nullable = false)
	private Task task;

	@Column(name = "file_name", nullable = false)
	private String fileName;

	@Column(name = "stored_file_name", nullable = false)
	private String storedFileName;

	@Column(name = "content_type")
	private String contentType;

	@Column(nullable = false)
	private long size;

	@Column(name = "uploaded_at", nullable = false)
	private LocalDateTime uploadedAt;

	public TaskAttachment() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	/**
	 * Удобство для кода без загрузки полной сущности {@link Task} (тесты, in-memory слой).
	 * Для сохранения через JPA предпочтительно {@link #setTask(Task)} с управляемой сущностью.
	 */
	public Long getTaskId() {
		return task == null ? null : task.getId();
	}

	public void setTaskId(Long taskId) {
		if (taskId == null) {
			this.task = null;
			return;
		}
		Task ref = new Task();
		ref.setId(taskId);
		this.task = ref;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getStoredFileName() {
		return storedFileName;
	}

	public void setStoredFileName(String storedFileName) {
		this.storedFileName = storedFileName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}

	public void setUploadedAt(LocalDateTime uploadedAt) {
		this.uploadedAt = uploadedAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TaskAttachment that = (TaskAttachment) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "TaskAttachment{" +
				"id=" + id +
				", taskId=" + getTaskId() +
				", fileName='" + fileName + '\'' +
				", storedFileName='" + storedFileName + '\'' +
				", contentType='" + contentType + '\'' +
				", size=" + size +
				", uploadedAt=" + uploadedAt +
				'}';
	}
}
