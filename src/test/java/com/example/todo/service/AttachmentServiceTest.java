package com.example.todo.service;

import com.example.todo.exception.TaskNotFoundException;
import com.example.todo.model.Task;
import com.example.todo.model.TaskAttachment;
import com.example.todo.repository.TaskAttachmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private TaskAttachmentRepository taskAttachmentRepository;
    @Mock
    private TaskService taskService;

    private Path tempDir;

    private AttachmentService attachmentService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        this.tempDir = tempDir;
        attachmentService = new AttachmentService(taskAttachmentRepository, taskService, tempDir.toString());
    }

    @Test
    void storeAttachment_savesFileAndMetadata() throws Exception {
        Task task = new Task();
        task.setId(1L);
        when(taskService.findById(1L)).thenReturn(Optional.of(task));
        TaskAttachment saved = new TaskAttachment();
        saved.setId(99L);
        when(taskAttachmentRepository.save(any(TaskAttachment.class))).thenAnswer(inv -> {
            TaskAttachment a = inv.getArgument(0);
            a.setId(99L);
            return a;
        });

        MultipartFile file = new MockMultipartFile("file", "doc.txt", "text/plain", "hello".getBytes());

        TaskAttachment result = attachmentService.storeAttachment(1L, file);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getTaskId()).isEqualTo(1L);
        assertThat(result.getFileName()).isEqualTo("doc.txt");
        assertThat(result.getContentType()).isEqualTo("text/plain");
        assertThat(result.getSize()).isEqualTo(5L);
        assertThat(result.getStoredFileName()).isNotBlank();

        ArgumentCaptor<TaskAttachment> captor = ArgumentCaptor.forClass(TaskAttachment.class);
        verify(taskAttachmentRepository).save(captor.capture());
        Path storedOnDisk = tempDir.resolve(captor.getValue().getStoredFileName());
        assertThat(Files.readString(storedOnDisk)).isEqualTo("hello");
    }

    @Test
    void storeAttachment_taskNotFound_throws() {
        when(taskService.findById(2L)).thenReturn(Optional.empty());

        MultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "x".getBytes());

        assertThatThrownBy(() -> attachmentService.storeAttachment(2L, file))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void storeAttachment_emptyFile_throws() {
        MultipartFile empty = new MockMultipartFile("file", "a.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> attachmentService.storeAttachment(1L, empty))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void getAttachmentsByTaskId_delegatesToRepository() {
        Task task = new Task();
        task.setId(3L);
        when(taskService.findById(3L)).thenReturn(Optional.of(task));
        TaskAttachment a = new TaskAttachment();
        a.setId(1L);
        when(taskAttachmentRepository.findByTask_Id(3L)).thenReturn(List.of(a));

        List<TaskAttachment> list = attachmentService.getAttachmentsByTaskId(3L);

        assertThat(list).hasSize(1);
    }

    @Test
    void getAttachmentsByTaskId_taskMissing_throws() {
        when(taskService.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.getAttachmentsByTaskId(3L))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void loadAsResource_returnsReadableResource() throws Exception {
        TaskAttachment meta = new TaskAttachment();
        meta.setStoredFileName("stored.bin");
        meta.setFileName("orig.bin");
        meta.setContentType("application/octet-stream");
        meta.setSize(3);
        when(taskAttachmentRepository.findById(10L)).thenReturn(Optional.of(meta));
        Files.writeString(tempDir.resolve("stored.bin"), "abc");

        Resource resource = attachmentService.loadAsResource(10L);

        assertThat(resource.getInputStream().readAllBytes()).isEqualTo("abc".getBytes());
    }

    @Test
    void deleteAttachment_removesFileAndRecord() throws Exception {
        TaskAttachment meta = new TaskAttachment();
        meta.setStoredFileName("to-delete.txt");
        when(taskAttachmentRepository.findById(5L)).thenReturn(Optional.of(meta));
        Files.writeString(tempDir.resolve("to-delete.txt"), "data");

        attachmentService.deleteAttachment(5L);

        verify(taskAttachmentRepository).deleteById(5L);
        assertThat(Files.exists(tempDir.resolve("to-delete.txt"))).isFalse();
    }
}
