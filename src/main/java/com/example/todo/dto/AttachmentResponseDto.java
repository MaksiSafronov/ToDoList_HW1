package com.example.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "AttachmentResponseDto", description = "Метаданные вложения после загрузки")
public class AttachmentResponseDto {

    @Schema(description = "Идентификатор вложения")
    private Long id;

    @Schema(description = "Исходное имя файла", example = "report.pdf")
    private String fileName;

    @Schema(description = "Размер в байтах", example = "1024")
    private long size;

    @Schema(description = "Время загрузки")
    private LocalDateTime uploadedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
}
