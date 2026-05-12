package com.example.todo.mapper;

import com.example.todo.dto.TaskCreateDto;
import com.example.todo.dto.TaskResponseDto;
import com.example.todo.dto.TaskUpdateDto;
import com.example.todo.dto.TaskWithAttachmentsResponseDto;
import com.example.todo.dto.AttachmentResponseDto;
import com.example.todo.model.Task;
import com.example.todo.model.TaskAttachment;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "completed", constant = "false")
    Task toEntity(TaskCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Task updateEntity(TaskUpdateDto dto, @MappingTarget Task task);

    @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
    TaskResponseDto toResponseDto(Task task);

    @InheritConfiguration(name = "toResponseDto")
    @Mapping(target = "attachments", source = "attachments")
    TaskWithAttachmentsResponseDto toResponseWithAttachments(Task task);

    @BeanMapping(unmappedSourcePolicy = ReportingPolicy.IGNORE)
    AttachmentResponseDto toAttachmentResponseDto(TaskAttachment attachment);
}
