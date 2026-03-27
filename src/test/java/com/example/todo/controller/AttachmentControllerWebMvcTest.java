package com.example.todo.controller;

import com.example.todo.exception.GlobalExceptionHandler;
import com.example.todo.exception.TaskNotFoundException;
import com.example.todo.model.TaskAttachment;
import com.example.todo.service.AttachmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AttachmentController.class)
@Import(GlobalExceptionHandler.class)
class AttachmentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttachmentService attachmentService;
    @MockBean
    private Environment environment;

    @Test
    void uploadAttachment_positive_returns201() throws Exception {
        TaskAttachment stored = new TaskAttachment();
        stored.setId(1L);
        stored.setFileName("f.txt");
        stored.setSize(4);
        stored.setUploadedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        when(attachmentService.storeAttachment(eq(2L), any())).thenReturn(stored);

        MockMultipartFile file = new MockMultipartFile(
                "file", "f.txt", MediaType.TEXT_PLAIN_VALUE, "data".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/tasks/2/attachments").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fileName").value("f.txt"))
                .andExpect(jsonPath("$.size").value(4));
    }

    @Test
    void uploadAttachment_taskNotFound_returns404() throws Exception {
        when(attachmentService.storeAttachment(eq(99L), any()))
                .thenThrow(new TaskNotFoundException(99L));

        MockMultipartFile file = new MockMultipartFile(
                "file", "f.txt", MediaType.TEXT_PLAIN_VALUE, "x".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/tasks/99/attachments").file(file))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.details.taskId").value(99));
    }

    @Test
    void downloadAttachment_positive_returnsFile() throws Exception {
        TaskAttachment meta = new TaskAttachment();
        meta.setFileName("report.pdf");
        meta.setContentType("application/pdf");
        meta.setSize(4);
        when(attachmentService.getAttachment(5L)).thenReturn(meta);
        Resource resource = new ByteArrayResource("pdf!".getBytes(StandardCharsets.UTF_8));
        when(attachmentService.loadAsResource(5L)).thenReturn(resource);

        mockMvc.perform(get("/api/attachments/5"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("report.pdf")))
                .andExpect(content().bytes("pdf!".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void deleteAttachment_positive_returns204() throws Exception {
        doNothing().when(attachmentService).deleteAttachment(3L);

        mockMvc.perform(delete("/api/attachments/3"))
                .andExpect(status().isNoContent());
    }

    @Test
    void listTaskAttachments_positive_returnsArray() throws Exception {
        TaskAttachment a = new TaskAttachment();
        a.setId(1L);
        a.setFileName("a.txt");
        a.setSize(1);
        a.setUploadedAt(LocalDateTime.of(2026, 2, 1, 0, 0));
        when(attachmentService.getAttachmentsByTaskId(7L)).thenReturn(List.of(a));

        mockMvc.perform(get("/api/tasks/7/attachments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fileName").value("a.txt"));
    }

    @Test
    void listTaskAttachments_taskNotFound_returns404() throws Exception {
        when(attachmentService.getAttachmentsByTaskId(8L))
                .thenThrow(new TaskNotFoundException(8L));

        mockMvc.perform(get("/api/tasks/8/attachments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
