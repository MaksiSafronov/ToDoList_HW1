package com.example.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "TaskWithAttachmentsResponseDto", description = "Задача со списком вложений")
public class TaskWithAttachmentsResponseDto extends TaskResponseDto {

	@Schema(description = "Вложения задачи")
	private List<AttachmentResponseDto> attachments;

	public List<AttachmentResponseDto> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentResponseDto> attachments) {
		this.attachments = attachments;
	}
}
