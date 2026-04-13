package com.example.todo.controller;

import com.example.todo.dto.TaskResponseDto;
import com.example.todo.exception.GlobalExceptionHandler;
import com.example.todo.mapper.TaskMapper;
import com.example.todo.model.Task;
import com.example.todo.service.FavoritesService;
import com.example.todo.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FavoritesController.class)
@Import(GlobalExceptionHandler.class)
class FavoritesControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoritesService favoritesService;
    @MockBean
    private TaskService taskService;
    @MockBean
    private TaskMapper taskMapper;

    @Test
    void addToFavorites_positive_returns200() throws Exception {
        Task task = new Task();
        task.setId(1L);
        when(taskService.findById(1L)).thenReturn(Optional.of(task));

        mockMvc.perform(post("/api/favorites/1").session(new MockHttpSession()))
                .andExpect(status().isOk());

        verify(favoritesService).addToFavorites(eq(1L), any());
    }

    @Test
    void addToFavorites_taskMissing_returns404() throws Exception {
        when(taskService.findById(404L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/favorites/404").session(new MockHttpSession()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void removeFromFavorites_positive_returns204() throws Exception {
        mockMvc.perform(delete("/api/favorites/2").session(new MockHttpSession()))
                .andExpect(status().isNoContent());

        verify(favoritesService).removeFromFavorites(eq(2L), any());
    }

    @Test
    void getFavorites_positive_returnsList() throws Exception {
        MockHttpSession session = new MockHttpSession();
        when(favoritesService.getFavoriteTaskIds(any())).thenReturn(List.of(10L, 11L));
        Task t10 = new Task();
        t10.setId(10L);
        t10.setTitle("A");
        when(taskService.findById(10L)).thenReturn(Optional.of(t10));
        Task t11 = new Task();
        t11.setId(11L);
        t11.setTitle("B");
        when(taskService.findById(11L)).thenReturn(Optional.of(t11));
        TaskResponseDto dto10 = new TaskResponseDto();
        dto10.setId(10L);
        dto10.setTitle("A");
        TaskResponseDto dto11 = new TaskResponseDto();
        dto11.setId(11L);
        dto11.setTitle("B");
        when(taskMapper.toResponseDto(t10)).thenReturn(dto10);
        when(taskMapper.toResponseDto(t11)).thenReturn(dto11);

        mockMvc.perform(get("/api/favorites").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[1].id").value(11));
    }

    @Test
    void getFavorites_emptySession_returnsEmptyArray() throws Exception {
        MockHttpSession session = new MockHttpSession();
        when(favoritesService.getFavoriteTaskIds(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/favorites").session(session))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
